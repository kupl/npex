package npex.strategy;

import spoon.reflect.code.CtExpression;

public class ReplaceEntireExpressionStrategy extends AbstractReplaceStrategy {

  public ReplaceEntireExpressionStrategy(ValueInitializer initializer) {
    super(initializer);
    this.name = "ReplaceEntireExpression" + initializer.getName();
  }

  public CtExpression<?> extractExprToReplace(CtExpression<?> nullExp) {
    return nullExp.getParent(CtExpression.class);
  }
}