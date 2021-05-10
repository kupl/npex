package npex.extractor.context;

import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtInvocation;

public class InvocationIsIsolated implements Context {
  public Boolean extract(CtInvocation invo, int nullPos) {
    return ASTUtils.getEnclosingStatement(invo).equals(invo);
  }
}