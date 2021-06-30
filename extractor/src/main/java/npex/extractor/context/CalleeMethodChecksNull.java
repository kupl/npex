package npex.extractor.context;

import java.util.function.Predicate;

import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtIf;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

public class CalleeMethodChecksNull extends AbstractCalleeMethodContext {
  protected boolean extract(CtExecutable callee, int nullPos) {
    Predicate<CtIf> pred = s -> s.getCondition()instanceof CtBinaryOperator cond && ASTUtils.isNullCondition(cond);
    return callee.getElements(new TypeFilter<>(CtIf.class)).stream().anyMatch(pred);
  }
}