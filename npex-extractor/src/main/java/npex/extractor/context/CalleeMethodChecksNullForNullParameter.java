package npex.extractor.context;

import java.util.List;
import java.util.function.Predicate;

import npex.common.filters.ConditionalExpressionFilter;
import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;

public class CalleeMethodChecksNullForNullParameter extends AbstractCalleeMethodContext {
  @Override
  public Boolean extract(CtExecutable callee, int nullPos) {
    if (nullPos == -1 || callee.getParameters().size() <= nullPos)
      return false;

    CtParameter nullParam = (CtParameter) callee.getParameters().get(nullPos);
    Predicate<CtExpression> accessParam = e -> e instanceof CtVariableAccess va && nullParam.equals(va.getVariable());
    Predicate<CtExpression<Boolean>> checksNullParam = cond -> cond instanceof CtBinaryOperator bo && ASTUtils.isNullCondition(bo)
        && (accessParam.test(bo.getLeftHandOperand()) || accessParam.test(bo.getRightHandOperand()));

    List<CtExpression<Boolean>> conditions = callee.getElements(new ConditionalExpressionFilter());
    return conditions.stream().anyMatch(checksNullParam);
  }

  private boolean isParameterAccess(CtExpression e, CtParameter param) {
    return e instanceof CtVariableAccess va && va.getVariable().equals(param);
  }
}