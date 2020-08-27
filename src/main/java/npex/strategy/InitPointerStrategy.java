package npex.strategy;

import java.util.List;
import java.util.stream.Collectors;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;

public class InitPointerStrategy extends AbstractStrategy {
  final protected ValueInitializer initializer;

  public InitPointerStrategy(ValueInitializer initializer) {
    this.initializer = initializer;
    this.name = "InitPointer" + initializer.getName();
  }

  public boolean isApplicable(CtExpression<?> nullExp) {
    if (!(nullExp instanceof CtVariableAccess || nullExp instanceof CtThisAccess))
      return false;
    return !initializer.getInitializerExpressions(nullExp).isEmpty();
  }

  protected <T, A extends T> CtAssignment<T, A> createAssignment(CtExpression<T> nullExp, CtExpression<A> value) {
    CtAssignment<T, A> assignment = nullExp.getFactory().createAssignment();
    assignment.setAssigned(nullExp);
    assignment.setAssignment(value);
    return assignment;
  }

  protected <T> List<CtElement> createInitializeStatements(CtExpression<T> nullExp) {
    List<CtExpression<? extends T>> values = initializer.getInitializerExpressions(nullExp);
    return values.stream().map(v -> createAssignment(nullExp.clone(), v)).collect(Collectors.toList());
  }

  @Override
  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return null;
  }

  List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    return createInitializeStatements(nullExp);
  }
}