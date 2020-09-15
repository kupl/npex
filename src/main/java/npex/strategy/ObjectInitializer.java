package npex.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

public class ObjectInitializer extends ValueInitializer {
  public String getName() {
    return "Object";
  }

  public <T> List<CtConstructorCall<T>> getConstructorCalls(CtExpression<?> nullExp, CtTypeReference<T> typ) {
    Factory factory = nullExp.getFactory();
    if (typ == null || !typ.isClass() || typ.isInterface()
        || typ.getDeclaration() != null && typ.getDeclaration().isAbstract()) {
      return new ArrayList<>();
    }

    List<CtConstructorCall<T>> calls = new ArrayList<>();
    calls.add(factory.createConstructorCall(typ));
    return calls;
  }

  public <T> List<CtExpression<? extends T>> getInitializerExpressions(CtExpression<?> nullExp,
      CtTypeReference<T> typ) {
    return getConstructorCalls(nullExp, typ).stream().collect(Collectors.toList());
  }
}