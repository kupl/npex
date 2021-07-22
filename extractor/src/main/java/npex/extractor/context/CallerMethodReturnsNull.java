package npex.extractor.context;

import npex.common.utils.FactoryUtils;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

public class CallerMethodReturnsNull extends AbstractCallerMethodContext {

	private boolean isEvaluatedToNull(CtExpression e) {
		if (e instanceof CtConditional ternary) {
			return isEvaluatedToNull(ternary.getThenExpression()) || isEvaluatedToNull(ternary.getElseExpression());
		}
		return e.equals(FactoryUtils.NULL_LIT);
	}

	protected boolean predicateOnMethod(CtExecutable exec) {
		return exec.getElements(new TypeFilter<>(CtReturn.class)).stream()
				.anyMatch(ret -> ret.getReturnedExpression() != null && isEvaluatedToNull(ret.getReturnedExpression()));
	}
}