package npex.extractor.context;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtAssignment;

public class SinkExprIsAssigned implements Context {
  public Boolean extract(CtAbstractInvocation invo, int nullPos) {
    return invo.getParent(CtAssignment.class) != null;
  }
}