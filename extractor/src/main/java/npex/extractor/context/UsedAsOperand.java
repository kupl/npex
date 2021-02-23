package npex.extractor.context;

import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtInvocation;

public class UsedAsOperand implements Context {
  public Boolean extract(CtInvocation invo, int nullPos) {
    return invo.getParent(CtBinaryOperator.class) != null;
  }

}