package npex.extractor.context;

import spoon.reflect.declaration.CtExecutable;

public class CalleeMethodReturnsVoid extends AbstractCalleeMethodContext {
  protected boolean extract(CtExecutable callee, int nullPos) {
    return callee.getType().equals("void");
  }
}