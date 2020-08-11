package npex.strategy;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;

public class SkipBreakStrategy extends SkipLoopStrategy {
  public SkipBreakStrategy() {
    this.name = "SkipBreak";
  }

  @Override
  protected CtStatement createNullBlockStmt(CtExpression<?> nullExp) {
    return nullExp.getFactory().createBreak();
  }

  @Override
  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return nullExp.getParent(CtLoop.class);
  }
}