package npex.strategy;

import java.util.Collections;
import java.util.stream.Stream;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

public class ObjectInitializer extends ValueInitializer<CtConstructorCall> {
  public String getName() {
    return "Object";
  }

  protected Stream<CtConstructorCall> enumerate(CtExpression expr) {
    CtTypeReference typ = expr.getType();
    if (typ == null || !typ.isClass() || typ.isInterface()
        || typ.getDeclaration() != null && typ.getDeclaration().isAbstract()) {
      return Stream.empty();
    }

    return Collections.singleton(expr.getFactory().createConstructorCall(typ)).stream();
  }
}