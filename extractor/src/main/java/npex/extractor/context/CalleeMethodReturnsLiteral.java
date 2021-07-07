package npex.extractor.context;

import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

public class CalleeMethodReturnsLiteral extends AbstractCalleeMethodContext {
  private boolean isEvaluatedToLiteral(CtExpression e) {
    if (e instanceof CtConditional ternary) {
      return isEvaluatedToLiteral(ternary.getThenExpression()) || isEvaluatedToLiteral(ternary.getElseExpression());
    }

    return e instanceof CtLiteral;
  }

  protected boolean extract(CtExecutable callee, int nullPos) {
    return callee.getElements(new TypeFilter<>(CtReturn.class)).stream()
        .anyMatch(ret -> isEvaluatedToLiteral(ret.getReturnedExpression()));
  }
}