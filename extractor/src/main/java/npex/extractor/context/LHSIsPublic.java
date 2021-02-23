package npex.extractor.context;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableWrite;

public class LHSIsPublic implements Context {
  public Boolean extract(CtInvocation invo, int nullPos) {
    if (invo.getParent(CtAssignment.class) instanceof CtAssignment assignment) {
      return assignment.getAssigned() instanceof CtVariableWrite write
          && write.getVariable().getDeclaration().isPublic();
    } else {
      return false;
    }
  }

}