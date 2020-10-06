package npex.strategy;

import java.util.Collections;
import java.util.List;

import npex.Utils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.SameFilter;

public abstract class SkipStrategy extends AbstractStrategy {
  protected List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    return Collections.singletonList(null);
  }

  public final boolean isApplicable(CtExpression<?> nullExp) {
    /* A skip strategy is basically inapplicable to null source */
    if (nullExp.toString().equals("null")) {
      return false;
    }

    /* Do not skip if null expr is a loop-head element */
    CtLoop loop = nullExp.getParent(CtLoop.class);
    if (loop != null) {
      CtElement loopHead = Utils.getLoopHeadElement(loop);
      if (loopHead != null && !loopHead.getElements(new SameFilter(nullExp)).isEmpty())
        return false;
    }

    return _isApplicable(nullExp);
  }

  /* Implement this method for an additional strategy-specific check */
  abstract protected boolean _isApplicable(CtExpression<?> nullExp);
}