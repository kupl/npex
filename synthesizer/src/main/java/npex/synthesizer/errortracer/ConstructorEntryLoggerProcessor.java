package npex.synthesizer.errortracer;

import java.io.File;

import org.apache.log4j.Logger;

import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.reference.CtExecutableReference;

public class ConstructorEntryLoggerProcessor extends AbstractLoggerProcessor<CtConstructor<?>> {
  protected Logger logger = Logger.getLogger(InvocationLoggerProcessor.class);

  public ConstructorEntryLoggerProcessor(File projectRoot) {
    super(projectRoot, "ENTRY");
  }

  boolean isInitializerInvocation(CtStatement stmt) {
    if (!(stmt instanceof CtExecutableReference))
      return false;
    CtExecutableReference<?> ex = (CtExecutableReference<?>) stmt;
    return ex.getSimpleName().equals("super") || ex.getSimpleName().equals("this");
  }

  @Override
  public void process(CtConstructor<?> e) {
    e.getBody().getStatement(1).insertBefore(createPrintStatement(e));
  }

  /*
   * Every constructor method has internal `super` or `this` initializers in
   * Spoon, and we don't instrument constructors consist of an initializer only.
   */
  boolean _isToBeProcessed(CtConstructor<?> e) {
    return e.getBody().getStatements().size() >= 2;
  }

  String getElementName(CtConstructor<?> e) {
    return e.getDeclaringType().getSimpleName();
  }
}