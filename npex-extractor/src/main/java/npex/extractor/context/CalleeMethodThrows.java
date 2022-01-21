package npex.extractor.context;

import spoon.reflect.declaration.CtExecutable;

public class CalleeMethodThrows extends AbstractCalleeMethodContext {
  @Override
  public Boolean extract(CtExecutable callee, int nullPos) {
    return !callee.getThrownTypes().isEmpty();
  }
}