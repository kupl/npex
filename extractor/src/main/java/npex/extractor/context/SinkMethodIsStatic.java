package npex.extractor.context;

import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;

public class SinkMethodIsStatic extends AbstractSinkMethodContext {
  protected boolean predicateOnMethod(CtExecutable exec) {
    return exec instanceof CtMethod mthd && mthd.isStatic();
  }
}