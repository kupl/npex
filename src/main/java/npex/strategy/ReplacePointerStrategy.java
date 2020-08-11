package npex.strategy;

import spoon.reflect.code.CtExpression;

public class ReplacePointerStrategy extends AbstractReplaceStrategy {
  public ReplacePointerStrategy(ValueInitializer initializer) {
    super(initializer);
    this.name = "ReplacePointer" + initializer.getName();
  }

  public CtExpression<?> extractExprToReplace(CtExpression<?> nullExp) {
    return nullExp;
  }
}