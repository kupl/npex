package npex.common.filters;

import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.AbstractFilter;

public class ConditionalExpressionFilter extends AbstractFilter<CtExpression<Boolean>> {
	@Override
	public boolean matches(CtExpression<Boolean> e) {
		CtElement parent = e.getParent();
		return parent instanceof CtConditional || parent instanceof CtIf;
	}

}