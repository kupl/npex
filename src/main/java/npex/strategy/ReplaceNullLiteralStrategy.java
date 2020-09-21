package npex.strategy;

import java.util.List;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;

public class ReplaceNullLiteralStrategy extends AbstractReplaceStrategy {
  public ReplaceNullLiteralStrategy(ValueInitializer initializer) {
    super(initializer);
    this.name = "ReplaceNullLiteral" + initializer.getName();
  }

  public boolean isApplicable(CtExpression<?> nullExp) {
    if (!nullExp.toString().equals("null"))
      return false;

    return nullExp.getParent(CtAssignment.class) != null;
  }

  CtExpression<?> extractExprToReplace(CtExpression<?> nullExp) {
    return nullExp;
  }

  <T> List<CtExpression<? extends T>> getInitializers(CtExpression<?> nullExp, CtLocalVariable<T> decl) {
    return initializer.getInitializerExpressions(nullExp, decl.getType());
  }

  @Override
  List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    return (List<CtElement>) getInitializers(nullExp, nullExp.getParent(CtLocalVariable.class));
  }

}