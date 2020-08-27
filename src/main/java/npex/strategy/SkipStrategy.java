package npex.strategy;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public abstract class SkipStrategy extends AbstractStrategy {
  protected List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    return new ArrayList<CtElement>();
  }

  abstract public boolean isApplicable(CtExpression<?> nullExp);
}