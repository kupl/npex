package npex.synthesizer.initializer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import npex.common.utils.ASTUtils;
import npex.common.utils.FactoryUtils;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

public class EqualityInitializer extends ValueInitializer<CtBinaryOperator> {
  public String getName() {
    return "Equal";
  }

  protected Stream<CtBinaryOperator> enumerate(CtExpression expr) {
    Factory factory = expr.getFactory();
    if (!(expr instanceof CtInvocation) || !expr.getType().equals(tf.BOOLEAN_PRIMITIVE))
      return Stream.empty();

    return ASTUtils.getInvocationArguments((CtInvocation) expr, true).stream()
        .filter(arg -> !arg.getType().isPrimitive()).map(arg -> {
          CtBinaryOperator binop = factory.createBinaryOperator();
          binop.setKind(BinaryOperatorKind.EQ);
          binop.setLeftHandOperand(arg.clone());
          binop.setRightHandOperand(FactoryUtils.createNullLiteral());
          binop.setType(tf.BOOLEAN_PRIMITIVE);
          return binop;
        });
  }

  protected CtExpression convertToCtExpression(CtBinaryOperator typedElement) {
    return typedElement;
  }

}