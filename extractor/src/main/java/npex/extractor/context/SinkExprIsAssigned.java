package npex.extractor.context;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtInvocation;

public class SinkExprIsAssigned implements Context {
  public Boolean extract(CtInvocation invo, int nullPos) {
    return invo.getParent(CtAssignment.class) != null;
  }
}