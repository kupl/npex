package npex.strategy;

import spoon.reflect.code.CtExpression;

public class ReplaceSinkExprStrategy extends AbstractReplaceStrategy {
  public ReplaceSinkExprStrategy(ValueInitializer initializer) {
    super(initializer);
    this.name = "ReplaceSinkExpr" + initializer.getName();
  }

  public CtExpression<?> extractExprToReplace(CtExpression<?> nullExp) {
    return nullExp.getParent(CtExpression.class);
  }
}