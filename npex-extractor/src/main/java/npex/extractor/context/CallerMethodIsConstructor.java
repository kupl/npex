package npex.extractor.context;

import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;

public class CallerMethodIsConstructor extends AbstractCallerMethodContext {
  protected boolean predicateOnMethod(CtExecutable exec) {
    return exec instanceof CtConstructor;
  }
}