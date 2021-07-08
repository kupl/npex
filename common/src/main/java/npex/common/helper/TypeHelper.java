package npex.common.helper;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

public class TypeHelper {
	public static CtTypeReference getTypeOfInvocation(CtAbstractInvocation invo) {
		CtTypeReference type = invo.getExecutable().getType();
		if (type != null) {
			return type;
		}

		if (invo.getParent()instanceof CtBinaryOperator e) {
			CtTypeReference lhsType = e.getLeftHandOperand().getType();
			CtTypeReference rhsType = e.getRightHandOperand().getType();
			return lhsType == null ? rhsType : lhsType;
		}

		return null;
	}

}