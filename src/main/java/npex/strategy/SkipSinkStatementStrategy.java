package npex.strategy;

import npex.Utils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.SameFilter;

public class SkipSinkStatementStrategy extends SkipStrategy {
  public SkipSinkStatementStrategy() {
    this.name = "SkipSinkStatement";
  }

  public boolean isApplicable(CtExpression<?> nullExp) {
    CtLoop loop = nullExp.getParent(CtLoop.class);
    if (loop == null)
      return true;
    CtElement loopHead = Utils.getLoopHeadElement(loop);
    logger.fatal(loopHead);
    return loopHead == null || loopHead.getElements(new SameFilter(nullExp)).isEmpty();
  }
}