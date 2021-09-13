package npex.extractor.context;

import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtAbstractInvocation;

public class InvocationIsIsolated implements Context {
  public Boolean extract(CtAbstractInvocation invo, int nullPos) {
    return ASTUtils.getEnclosingStatement(invo).equals(invo);
  }
}