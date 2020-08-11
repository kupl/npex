package npex.strategy;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;

public abstract class SkipStrategy extends AbstractStrategy {
  protected CtStatement createNullBlockStmt(CtExpression<?> nullExp) {
    return null;
  }

  abstract public boolean isApplicable(CtExpression<?> nullExp);
}