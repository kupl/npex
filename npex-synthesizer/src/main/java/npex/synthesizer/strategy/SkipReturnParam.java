package npex.synthesizer.strategy;

import npex.common.filters.MethodOrConstructorFilter;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtParameter;

public class SkipReturnParam extends SkipReturnStrategy {

  @Override
  protected CtStatement createSkipFrom(CtExpression nullExp) {
    return nullExp.getParent(new MethodOrConstructorFilter()).getBody().getStatements().get(0);
  }

  @Override
  public boolean _isApplicable(CtExpression nullExp) {
    return super._isApplicable(nullExp) && nullExp instanceof CtVariableAccess va
        && va.getVariable().getDeclaration() instanceof CtParameter;
  }
}