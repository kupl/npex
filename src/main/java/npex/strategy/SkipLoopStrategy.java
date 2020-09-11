package npex.strategy;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLoop;

public abstract class SkipLoopStrategy extends SkipStrategy {
  public boolean _isApplicable(CtExpression<?> nullExp) {
    return nullExp.getParent(CtLoop.class) != null;
  }
}