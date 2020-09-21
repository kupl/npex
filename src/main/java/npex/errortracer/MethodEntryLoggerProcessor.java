package npex.errortracer;

import java.io.File;

import org.apache.log4j.Logger;

import spoon.reflect.declaration.CtMethod;

public class MethodEntryLoggerProcessor extends AbstractLoggerProcessor<CtMethod<?>> {
  protected Logger logger = Logger.getLogger(InvocationLoggerProcessor.class);

  public MethodEntryLoggerProcessor(File projectRoot) {
    super(projectRoot, "ENTRY");
  }

  @Override
  public void process(CtMethod<?> e) {
    e.getBody().insertBegin(createPrintStatement(e));
  }

  /* A method body can be null: e.g., interface */
  boolean _isToBeProcessed(CtMethod<?> e) {
    return e.getBody() != null;
  }

  String getElementName(CtMethod<?> e) {
    return e.getSimpleName();
  }
}