package npex.strategy;

import java.util.stream.Stream;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.TypeFilter;

@SuppressWarnings("rawtypes")
public class VarInitializer extends ValueInitializer<CtVariableAccess> {
  public String getName() {
    return "Var";
  }

  protected Stream<CtVariableAccess> enumerate(CtExpression expr) {
    Stream<CtVariable> localVars = expr.getParent(CtMethod.class).getElements(new TypeFilter<>(CtVariable.class))
        .stream();
    Stream<CtVariable> classMembers = expr.getParent(CtClass.class).getAllFields().stream()
        .map(f -> f.getDeclaration());
    Stream<CtVariable> allVars = Stream.concat(localVars, classMembers).filter(v -> v != null);
    Factory factory = expr.getFactory();
    return allVars.map(v -> factory.createVariableRead(v.getReference(), false));
  }
}