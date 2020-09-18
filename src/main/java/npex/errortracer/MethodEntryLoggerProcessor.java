package npex.errortracer;

import org.apache.log4j.Logger;

import spoon.reflect.declaration.CtMethod;

public class MethodEntryLoggerProcessor extends AbstractLoggerProcessor<CtMethod<?>> {
  protected Logger logger = Logger.getLogger(InvocationLoggerProcessor.class);

  public MethodEntryLoggerProcessor() {
    super("ENTRY");
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