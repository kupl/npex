package npex.strategy;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;

public class SkipContinueStrategy extends SkipLoopStrategy {
  public SkipContinueStrategy() {
    this.name = "SkipContinue";
  }

  @Override
  protected CtStatement createNullBlockStmt(CtExpression<?> nullExp) {
    return nullExp.getFactory().createContinue();
  }

  @Override
  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    CtBlock<?> loopBody = (CtBlock<?>) nullExp.getParent(CtLoop.class).getBody();
    return loopBody.getLastStatement();
  }
}