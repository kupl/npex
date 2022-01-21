package npex.synthesizer.enumerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.CommonExpressionTable;
import npex.common.utils.ASTUtils;
import npex.common.utils.FactoryUtils;
import npex.common.utils.TypeUtil;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtActualTypeContainer;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

public class ExpressionEnumerator {
  final static Logger logger = LoggerFactory.getLogger(ExpressionEnumerator.class);

	public static List<CtExpression> enumTypeCompatibleExpressions(CtExpression expr, CtTypeReference typ) {
		/* Cannot proceed with no type information; stop the enumeration */
		if (typ == null) {
			return Collections.emptyList();
		}

		Factory factory = typ.getFactory();
    /* We remove the type arguments in a generic type because it disturbs subtpye checking.
    Simply ignore them and leave it to compiler */
    final CtTypeReference _typ;
    if (typ instanceof CtActualTypeContainer container) {
      _typ = typ.clone();
      _typ.setActualTypeArguments(new ArrayList<>());
    } else {
      _typ = typ;
    }

		List<CtExpression> exprs = new ArrayList<>();
		/* For boolean type, add equlity check for candidate */
		if (expr instanceof CtInvocation invo && typ.equals(TypeUtil.BOOLEAN_PRIMITIVE)) {
			List<CtBinaryOperator> equalities = ASTUtils.getInvocationArguments(invo, true).stream()
					.filter(arg -> !arg.getType().isPrimitive()).map(arg -> {
						CtBinaryOperator binop = factory.createBinaryOperator();
						binop.setKind(BinaryOperatorKind.EQ);
						binop.setLeftHandOperand(arg.clone());
						binop.setRightHandOperand(FactoryUtils.createNullLiteral());
						binop.setType(TypeUtil.BOOLEAN_PRIMITIVE);
						return binop;
					}).collect(Collectors.toList());
			exprs.addAll(equalities);
		}
 
		/* top-3 common expressions */
		exprs.addAll(CommonExpressionTable.find(_typ));

		if (_typ.isPrimitive()) {
			return exprs;
		}

		/* For non primitive types, add a null literal and default constructor for candidates if available  */
		exprs.add(FactoryUtils.createNullLiteral());
		if (_typ instanceof CtArrayTypeReference) {
			CtConstructorCall ctor = factory.createConstructorCall(_typ);
			CtNewArray newArr = factory.createNewArray();
			newArr.setType(ctor.getType());
			exprs.add(newArr);
		} else if (_typ.getDeclaration() instanceof CtClass klass && klass.getConstructor() != null) {
			exprs.add(factory.createConstructorCall(_typ));
		}
		
		return exprs;
	}

}