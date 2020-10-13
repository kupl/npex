package npex.errortracerjp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import org.apache.log4j.Logger;

public class ErrorTracerDriverJP {
  protected final Logger logger = Logger.getLogger(ErrorTracerDriverJP.class);
  private final ProjectRoot projectRoot;

  private final List<CompilationUnit> allCompilationUnits;

  private final ModifierVisitor<Void> traceVisitor;

  Stream<CompilationUnit> collectParsedSources(SourceRoot root) {
    List<CompilationUnit> results = new ArrayList<>();
    try {
      for (ParseResult<CompilationUnit> res : root.tryToParse()) {
        try {
          results.add(res.getResult().get());
        } catch (NoSuchElementException e) {
          res.getProblems().forEach(prob -> logger.fatal(prob.getMessage()));
        }
      }
    } catch (IOException e) {
      logger.fatal("Could not collect parsed source in root: " + root.getRoot());
      return Stream.empty();
    } catch (StackOverflowError e) {
      logger.fatal("StackOverflow in parsing root: " + root.getRoot());
    }
    return results.stream();
  }

  public ErrorTracerDriverJP(final String projectRootPath, String javaLanguageLevel) {
    ParserCollectionStrategy collectionStrategy = new ParserCollectionStrategy();
    collectionStrategy.getParserConfiguration()
        .setLanguageLevel(LanguageLevel.valueOf(String.format("JAVA_%s", javaLanguageLevel)));
    this.projectRoot = collectionStrategy.collect(Paths.get(projectRootPath));
    this.traceVisitor = new TraceVisitor(new File(projectRootPath));
    this.allCompilationUnits = projectRoot.getSourceRoots().stream().flatMap(root -> collectParsedSources(root))
        .collect(Collectors.toList());
    logger.debug("Javaparser - Java Language Level: " + collectionStrategy.getParserConfiguration().getLanguageLevel());
  }

  public void run() {
    this.allCompilationUnits.forEach(cu -> traceVisitor.visit(cu, null));
    this.allCompilationUnits.forEach(cu -> cu.getStorage().get().save());
  }
}