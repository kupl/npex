package npex.strategy;

import java.util.List;

import npex.Utils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public class ReplaceEntireExpressionStrategy extends AbstractReplaceStrategy {

  public ReplaceEntireExpressionStrategy(ValueInitializer initializer) {
    super(initializer);
    this.name = "ReplaceEntireExpression" + initializer.getName();
  }

  public boolean isApplicable(CtExpression nullExp) {
    return !nullExp.toString().equals("null");
  }

  CtExpression<?> extractExprToReplace(CtExpression<?> nullExp) {
    return Utils.getOutermostExpression(nullExp);
  }

  List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    CtExpression exprToRep = extractExprToReplace(nullExp);
    return initializer.getTypeCompatibleExpressions(exprToRep, exprToRep.getType());
  }
}