package npex.strategy;

import spoon.reflect.code.CtExpression;

public class SkipSinkStatementStrategy extends SkipStrategy {
  public SkipSinkStatementStrategy() {
    this.name = "SkipSinkStatement";
  }

  public boolean _isApplicable(CtExpression<?> nullExp) {
    return true;
  }
}