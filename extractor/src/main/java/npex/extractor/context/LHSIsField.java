package npex.extractor.context;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtFieldWrite;

public class LHSIsField implements Context {
  public Boolean extract(CtAbstractInvocation invo, int nullPos) {
    if (invo.getParent(CtAssignment.class)instanceof CtAssignment assignment) {
      return assignment.getAssigned() instanceof CtFieldWrite;
    } else {
      return false;
    }
  }

}