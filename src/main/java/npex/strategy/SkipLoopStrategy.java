package npex.strategy;

import npex.Utils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.SameFilter;

public abstract class SkipLoopStrategy extends SkipStrategy {
  protected boolean condChecksNull() {
    return true;
  }

  private CtLoop getEnclosingLoop(CtElement nullExp) {
    return nullExp.getParent(CtLoop.class);
  }

  @Override
  public boolean isApplicable(CtExpression<?> nullExp) {
    CtLoop loop = getEnclosingLoop(nullExp);
    if (loop == null)
      return false;
    CtElement loopHead = Utils.getLoopHeadElement(loop);
    return loopHead == null || loopHead.getElements(new SameFilter(nullExp)).isEmpty();
  }
}