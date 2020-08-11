package npex.buggycode;

import org.apache.log4j.Logger;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.reference.CtTypeReference;

public abstract class NullHandle {
  final CtStatement handle;
  final CtBinaryOperator<Boolean> cond;
  final CtBlock<?> parentBlock;

  Logger logger = Logger.getLogger(NullHandle.class);

  protected static boolean matches(CtExpression<Boolean> cond) {
    if (!(cond instanceof CtBinaryOperator))
      return false;
    try {
      CtBinaryOperator<Boolean> binop = (CtBinaryOperator<Boolean>) cond;
      BinaryOperatorKind kind = binop.getKind();
      if (!(kind.equals(BinaryOperatorKind.EQ) || kind.equals(BinaryOperatorKind.NE))) {
        return false;
      }
      String nullName = CtTypeReference.NULL_TYPE_NAME;
      CtTypeReference<?> leftType = binop.getLeftHandOperand().getType();
      CtTypeReference<?> rightType = binop.getRightHandOperand().getType();
      return leftType.toString().equals(nullName) || rightType.toString().equals(nullName);
    } catch (NullPointerException e) {
      return false;
    }
  }

  public NullHandle(CtStatement handle, CtExpression<Boolean> cond) {
    if (!matches(cond)) {
      logger.debug(handle + "does not match!");
      throw new IllegalArgumentException();
    }

    this.handle = handle;
    this.cond = (CtBinaryOperator<Boolean>) cond;
    this.parentBlock = handle.getParent(CtBlock.class);
  }

  boolean isCondKindEqual() {
    return cond.getKind().equals(BinaryOperatorKind.EQ);
  }

  public CtExpression<?> getNullPointer() {
    CtExpression<?> left = cond.getLeftHandOperand();
    CtExpression<?> right = cond.getRightHandOperand();
    return left.getType().toString().equals(CtTypeReference.NULL_TYPE_NAME) ? right : left;
  }

  public CtStatement getStatement() {
    return this.handle;
  }

  abstract public void stripNullHandle(CtClass<?> klass);
}