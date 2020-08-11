package npex.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

public class ObjectInitializer extends ValueInitializer {
  public String getName() {
    return "Object";
  }

  public <T> List<CtConstructorCall<T>> getConstructorCalls(CtExpression<T> nullExp) {
    Factory factory = nullExp.getFactory();
    CtType<T> typ = nullExp.getType().getTypeDeclaration();
    if (typ == null || !typ.isClass() || typ.isInterface() || typ.isAbstract()) {
      return new ArrayList<>();
    }
    List<CtConstructorCall<T>> calls = new ArrayList<>();
    calls.add(factory.createConstructorCall(typ.getReference()));
    return calls;
  }

  public <T> List<CtExpression<? extends T>> getInitializerExpressions(CtExpression<T> nullExp) {
    return (getConstructorCalls(nullExp)).stream().collect(Collectors.toList());
  }
}