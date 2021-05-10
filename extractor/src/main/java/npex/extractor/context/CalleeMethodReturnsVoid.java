package npex.extractor.context;

import spoon.reflect.declaration.CtExecutable;

public class CalleeMethodReturnsVoid extends AbstractCalleeMethodContext {
  protected boolean predicateOnMethod(CtExecutable callee) {
    return callee.getType().equals("void");
  }

}