package npex.extractor.context;

import java.util.List;

import npex.common.filters.ConditionalExpressionFilter;
import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtExecutable;
public class CalleeMethodChecksNull extends AbstractCalleeMethodContext {
  protected boolean extract(CtExecutable callee, int nullPos) {
    List<CtExpression<Boolean>> conditions = callee.getElements(new ConditionalExpressionFilter());
    return conditions.stream().anyMatch(c -> c instanceof CtBinaryOperator bo && ASTUtils.isNullCondition(bo));
  }
}