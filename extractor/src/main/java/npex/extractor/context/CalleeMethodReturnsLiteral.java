package npex.extractor.context;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

public class CalleeMethodReturnsLiteral extends AbstractCalleeMethodContext {
  protected boolean extract(CtExecutable callee, int nullPos) {
    return callee.getElements(new TypeFilter<>(CtReturn.class)).stream()
        .anyMatch(ret -> ret.getReturnedExpression() instanceof CtLiteral);
  }
}