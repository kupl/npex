package npex.strategy;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public class SkipBlockStrategy extends SkipStrategy {
  public SkipBlockStrategy() {
    this.name = "SkipBlock";
  }

  public boolean _isApplicable(CtExpression<?> nullExp) {
    return nullExp.getParent(CtBlock.class).getParent(CtBlock.class) != null;
  }

  @Override
  protected CtElement createSkipFrom(CtExpression<?> nullExp) {
    return nullExp.getParent(CtBlock.class);
  }

  @Override
  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return nullExp.getParent(CtBlock.class);
  }
}