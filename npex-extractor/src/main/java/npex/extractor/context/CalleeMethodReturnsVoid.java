package npex.extractor.context;

import npex.common.utils.TypeUtil;
import spoon.reflect.declaration.CtExecutable;

public class CalleeMethodReturnsVoid extends AbstractCalleeMethodContext {
  @Override
  public Boolean extract(CtExecutable callee, int nullPos) {
    return callee.getType().equals(TypeUtil.VOID_PRIMITIVE);
  }
}