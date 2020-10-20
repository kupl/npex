package npex.synthesizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import npex.synthesizer.errortracerjp.ErrorTracerDriverJP;

public class ErrorTracerDriverJPTest {

  final String projectRootPath = "/media/4tb/npex/NPEX_DATA/webmagic-ff2f588-buggy";
  ErrorTracerDriverJP driver;
  final Logger logger = Logger.getLogger(ErrorTracerDriverJPTest.class);
  File projectRoot;

  @Before
  public void setup() throws FileNotFoundException {
    this.projectRoot = new File(projectRootPath);
    this.driver = new ErrorTracerDriverJP(projectRootPath, "8");
  }

  @Test
  public void testRun() {
    driver.run();
  }

  @Test
  public void printAST() {
    String sourcePath = "src/test/java/org/springframework/data/repository/config/RepositoryBeanNameGeneratorUnitTests.java";
    // String path = String.format("%s/%s", projectRootPath, sourcePath);
    String path = "/home/june/tmp/caller-exp-multi/Java_exn1.java";
    try {
      CompilationUnit cu = StaticJavaParser.parse(new File(path));
      PrinterVisitor vs = new PrinterVisitor();
      vs.visit(cu, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  class PrinterVisitor extends ModifierVisitor<Void> {
    private Statement createPrintStatement(Node n, String tag, String name) {
      CompilationUnit cu = n.findCompilationUnit().get();
      File cuFile = cu.getStorage().get().getPath().toFile();
      String path = projectRoot.toURI().relativize(cuFile.toURI()).getPath();

      String loggerArguments = String.format("[%s] Filepath: %s, Line: %d, Element: %s", tag, path,
          n.getBegin().get().line, name);
      String loggingString = String.format("System.out.println(\"%s\");", loggerArguments);
      logger.info(loggerArguments);
      return StaticJavaParser.parseStatement(loggingString);
    }

    @Override
    public IfStmt visit(final IfStmt n, final Void arg) {
      Statement thenStmt = n.getThenStmt();
      Optional<Statement> elseStmt = n.getElseStmt();
      System.out.println("=== Visiting IfStmt at line: " + n.getBegin().get().line);
      System.out.println(n);
      System.out.println(thenStmt);
      elseStmt.ifPresent(el -> System.out.println(el));

      return n;
    }

    private Statement createPrintStatement(final MethodDeclaration n) {
      return createPrintStatement(n, "ENTRY", n.getNameAsString());
    }

    private Statement createPrintStatement(final MethodCallExpr n) {
      return createPrintStatement(n, "CALLSITE", n.getNameAsString());
    }

    private Statement createPrintStatement(final ObjectCreationExpr n) {
      return createPrintStatement(n, "CALLSITE", n.getTypeAsString());
    }

    public MethodCallExpr visit(final MethodCallExpr n, final Void arg) {
      System.out.println("Visting method call expr of " + n.getNameAsString());
      logger.fatal("Visiting invocation of " + n.getName() + "at line " + n.getBegin().get().line);
      n.findAncestor(ExplicitConstructorInvocationStmt.class)
          .ifPresent(c -> logger.fatal("-- has constructor call " + c + "at line "));
      return n;
    }

    @Override
    public ObjectCreationExpr visit(final ObjectCreationExpr n, final Void arg) {
      Statement loggingStmt = createPrintStatement(n);
      System.out.println("Visting " + n.getType());
      Optional<Statement> parentStmtOpt = n.findAncestor(Statement.class);
      if (!parentStmtOpt.isPresent()) {
        logger.fatal(n + " " + n.getTypeAsString() + " has no ancestor stmt");
        return n;
      }
      n.findAncestor(BlockStmt.class).get().getStatements().addBefore(loggingStmt, parentStmtOpt.get());
      return n;
    }

    @Override
    public MethodDeclaration visit(final MethodDeclaration n, final Void arg) {
      System.out.println("Visting method declaration of " + n.getNameAsString());
      if (n.isConstructorDeclaration() || !n.getBody().isPresent()) {
        return n;
      }

      Statement loggingStmt = createPrintStatement(n);
      n.getBody().get().addStatement(0, loggingStmt);
      logger.info(loggingStmt);
      return n;
    }
  }
}