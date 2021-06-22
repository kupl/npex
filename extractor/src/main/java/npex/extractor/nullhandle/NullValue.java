package npex.extractor.nullhandle;

import npex.common.NPEXException;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.factory.TypeFactory;

public class NullValue {
  final private CtExpression expr;
  final private CtAbstractInvocation invo;

  final static CtTypeReference throwableType = (new TypeFactory()).createReference(java.lang.Throwable.class);

  public NullValue(CtExpression expr, CtAbstractInvocation invo) {
    this.expr = expr;
    this.invo = invo;
  }

  public String getRawValue() {
    return expr.toString();
  }

  public String getAbstractValue() throws NPEXException {
    if (expr == null) {
      throw new NPEXException(
          String.format("Cannot extract null values for model %s invoaction infomation is incomplete!", this));
    }

    CtTypeReference type = expr.getType();
    if (type == null) {
      throw new NPEXException(String.format("Cannot extract null values for model %s its value type is null", this));
    }

    if (type.isSubtypeOf(throwableType)) {
      return "NPEXThrowable";
    }

    if (type.isGenerics()) {
      throw new NPEXException(
          String.format("Cannot extract null values for model %s its value type is generics", this));
    }
    CtTypeReference invoRetType = invo.getExecutable().getType();
    if (invoRetType == null) {
      throw new NPEXException(
          String.format("Cannot extract null values for model %s return type of invocation is null", this));
    }
    if (!expr.toString().equals("null") && !type.isSubtypeOf(invoRetType)) {
      throw new NPEXException(
          String.format("Cannot extract null values for model %s: %s is not a subtype of %s", this, type, invoRetType));
    }

    if (expr.toString().equals("null") && invoRetType.isPrimitive()) {
      throw new NPEXException(String
          .format("Invocation's return type is primitive but null literal is collected as null value for %s", this));
    }

    if (expr instanceof CtLiteral) {
      return expr.toString();
    } else if (expr instanceof CtConstructorCall ccall) {
      return ccall.getArguments().isEmpty() ? "NPEXDefaultNew" : "NPEXNonDefaultNew";
    } else {
      return "NPEXNonLiteral";
    }
  }
}