package npex.extractor.context;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtInvocation;

public class UsedAsOperand implements Context {
  public Boolean extract(CtAbstractInvocation invo, int nullPos) {
    return invo.getParent(CtBinaryOperator.class) != null;
  }

}