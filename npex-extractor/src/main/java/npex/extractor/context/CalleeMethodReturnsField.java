package npex.extractor.context;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

public class CalleeMethodReturnsField extends AbstractCalleeMethodContext {
  @Override
  public Boolean extract(CtExecutable callee, int nullPos) {
    return callee.getElements(new TypeFilter<>(CtReturn.class)).stream()
        .anyMatch(ret -> ret.getReturnedExpression() instanceof CtFieldAccess);
  }
}