package npex.extractor.context;

import java.util.function.Predicate;

import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.visitor.filter.TypeFilter;

public class CalleeMethodChecksNullForNullParameter extends AbstractCalleeMethodContext {
  protected boolean extract(CtExecutable callee, int nullPos) {
    if (nullPos == -1 || callee.getParameters().size() <= nullPos)
      return false;
    CtParameter nullParam = (CtParameter) callee.getParameters().get(nullPos);
    Predicate<CtIf> pred = s -> s.getCondition()instanceof CtBinaryOperator cond && ASTUtils.isNullCondition(cond)
        && (isParameterAccess(cond.getLeftHandOperand(), nullParam)
            || isParameterAccess(cond.getRightHandOperand(), nullParam));
    return callee.getElements(new TypeFilter<>(CtIf.class)).stream().anyMatch(pred);
  }

  private boolean isParameterAccess(CtExpression e, CtParameter param) {
    return e instanceof CtVariableAccess va && va.getVariable().equals(param);
  }
}