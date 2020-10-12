package npex.errortracerjp;

import java.io.File;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import org.apache.log4j.Logger;

import npex.errortracer.AbstractLoggerProcessor;

public class TraceVisitor extends ModifierVisitor<Void> {
  final protected Logger logger = Logger.getLogger(AbstractLoggerProcessor.class);

  final protected File projectRoot;

  public TraceVisitor(File projectRoot) {
    this.projectRoot = projectRoot;
  }

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

  private Statement createPrintStatement(final MethodDeclaration n) {
    return createPrintStatement(n.getName(), "ENTRY", n.getNameAsString());
  }

  /*
   * private Statement createPrintStatement(final MethodCallExpr n) { return
   * createPrintStatement(n, "CALLSITE", n.getNameAsString()); }
   * 
   * private Statement createPrintStatement(final ObjectCreationExpr n) { return
   * createPrintStatement(n, "CALLSITE", n.getTypeAsString()); }
   */

  @Override
  public MethodDeclaration visit(final MethodDeclaration n, final Void arg) {
    if (n.isConstructorDeclaration() || !n.getBody().isPresent()) {
      File cuFile = n.findCompilationUnit().get().getStorage().get().getPath().toFile();
      logger.fatal(String.format("%s: could not log at %d in %s", n.getNameAsString(), n.getBegin().get().line,
          cuFile.getPath()));
      return n;
    }

    Statement loggingStmt = createPrintStatement(n);
    n.getBody().get().addStatement(0, loggingStmt);
    logger.info(loggingStmt);
    return n;
  }

  /*
   * @Override public ObjectCreationExpr visit(final ObjectCreationExpr n, final
   * Void arg) { Statement loggingStmt = createPrintStatement(n);
   * Optional<Statement> parentStmtOpt = n.findAncestor(Statement.class); if
   * (!parentStmtOpt.isPresent()) { logger.fatal(n + " " + n.getTypeAsString() +
   * " has no ancestor stmt"); return n; }
   * n.findAncestor(BlockStmt.class).get().getStatements().addBefore(loggingStmt,
   * parentStmtOpt.get()); return n; }
   */

  /*
   * @Override public MethodCallExpr visit(final MethodCallExpr n, final Void arg)
   * { Statement loggingStmt = createPrintStatement(n); Node parent =
   * n.getParentNode().get();
   * 
   * logger.fatal("Visting " + n.getName() + " at " + n.getBegin().get().line);
   * 
   * if (parent instanceof LambdaExpr) { Optional<Expression> bodyExprOpt =
   * ((LambdaExpr) parent).getExpressionBody(); BlockStmt blk; if
   * (bodyExprOpt.isPresent()) { if (n.resolve().getReturnType().isVoid()) { blk =
   * StaticJavaParser.parseBlock(String.format("{%s; %s;}", loggingStmt, n)); }
   * else { blk = StaticJavaParser.parseBlock(String.format("{%s; return %s;}",
   * loggingStmt, n)); } ((LambdaExpr) parent).setBody(blk); } return n; }
   * 
   * if (n.findAncestor(ExplicitConstructorInvocationStmt.class).isPresent()) {
   * logger.fatal(n + " is an argument of constructor call"); return n; }
   * 
   * if (!n.findAncestor(BlockStmt.class).isPresent()) { logger.fatal(n); return
   * n; } for (Statement s :
   * n.findAncestor(BlockStmt.class).get().getStatements()) { System.out.println(s
   * + " " + s.getClass()); }
   * 
   * Optional<Statement> parentStmtOpt = n.findAncestor(Statement.class); if
   * (!parentStmtOpt.isPresent()) { logger.fatal(n + " " +
   * "has no ancestor stmt"); return n; }
   * 
   * n.findAncestor(BlockStmt.class).get().getStatements().addBefore(loggingStmt,
   * parentStmtOpt.get()); logger.info((n.getBegin().get().line));
   * logger.info(loggingStmt); logger.info((n.getBegin().get().line));
   * 
   * return n; }
   */

}
