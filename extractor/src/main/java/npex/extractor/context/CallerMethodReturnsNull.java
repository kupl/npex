package npex.extractor.context;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.filter.TypeFilter;

public class CallerMethodReturnsNull extends AbstractCallerMethodContext {

	protected boolean predicateOnMethod(CtExecutable exec) {
		return exec.getElements(new TypeFilter<>(CtReturn.class)).stream()
				.anyMatch(ret -> ret.getReturnedExpression() instanceof CtLiteral lit && lit.toString().equals("null"));
	}

}