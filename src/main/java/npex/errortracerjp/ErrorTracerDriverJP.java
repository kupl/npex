package npex.errortracerjp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    try {
      return root.tryToParse().stream().map(res -> res.getResult().get());
    } catch (IOException e) {
      logger.fatal(e.getMessage());
      return Stream.empty();
    }
  }

  public ErrorTracerDriverJP(final String projectRootPath) {
    ParserCollectionStrategy collectionStrategy = new ParserCollectionStrategy();
    this.projectRoot = collectionStrategy.collect(Paths.get(projectRootPath));
    this.traceVisitor = new TraceVisitor(new File(projectRootPath));
    this.allCompilationUnits = projectRoot.getSourceRoots().stream().flatMap(root -> collectParsedSources(root))
        .collect(Collectors.toList());

  }

  public void run() {
    this.allCompilationUnits.forEach(cu -> traceVisitor.visit(cu, null));
    this.allCompilationUnits.forEach(cu -> cu.getStorage().get().save());
  }
}