package npex.common.helper;

import npex.common.utils.TypeUtil;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtTypeReference;

public class TypeHelper {
	public static CtTypeReference getType(CtElement e) {
		if (e instanceof CtTypedElement te && te.getType() != null) {
			return te.getType();
		}

		CtElement parent = e.getParent();
		if (parent instanceof CtBinaryOperator bo) {
			return resolve(bo);
		}

		if (parent instanceof CtConditional ternary) {
			return resolve(ternary);
		}

		if (parent instanceof CtAssignment assign) {
			return resolve(assign);
		}

		return null;
	}


	private static CtTypeReference resolve(CtBinaryOperator e) {
		BinaryOperatorKind kind = e.getKind();
		if (kind.equals(BinaryOperatorKind.OR) || kind.equals(BinaryOperatorKind.AND)) {
			return TypeUtil.BOOLEAN_PRIMITIVE;
		}
		return null;
	}


	private static CtTypeReference resolve(CtConditional e) {
		CtTypeReference thenType = e.getThenExpression().getType();
		CtTypeReference elseType = e.getElseExpression().getType();

		if (thenType != null) {
			return thenType;
		} else if (elseType != null) {
			return elseType;
		} else if (e.getParent() instanceof CtReturn ret && ret.getParent(CtMethod.class) instanceof CtMethod mthd) {
			return mthd.getType();
		} else {
			return null;
		}
	}

	private static CtTypeReference resolve(CtAssignment e) {
		return e.getAssigned().getType();
	}
}