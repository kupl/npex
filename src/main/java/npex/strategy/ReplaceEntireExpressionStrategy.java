package npex.strategy;

import java.util.List;

import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public class ReplaceEntireExpressionStrategy extends AbstractReplaceStrategy {

  public ReplaceEntireExpressionStrategy(ValueInitializer initializer) {
    super(initializer);
    this.name = "ReplaceEntireExpression" + initializer.getName();
  }

  public boolean isApplicable(CtExpression nullExp) {
    if (extractExprToReplace(nullExp).getType().toString().equals("void"))
      return false;
    return !nullExp.toString().equals("null") && (nullExp.getParent(CtExpression.class) != null);
  }

  CtExpression<?> extractExprToReplace(CtExpression<?> nullExp) {
    return nullExp.getParent(CtExpression.class);
  }

  List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    return initializer.getTypeCompatibleExpressions(extractExprToReplace(nullExp),
        extractExprToReplace(nullExp).getType());
  }
}