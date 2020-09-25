package npex.strategy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;

public class ReplacePointerStrategy extends AbstractReplaceStrategy {
  public ReplacePointerStrategy(ValueInitializer initializer) {
    super(initializer);
    this.name = "ReplacePointer" + initializer.getName();
  }

  public boolean isApplicable(CtExpression nullExp) {
    return !isLiteralNull(nullExp) || nullExp.getParent() instanceof CtLocalVariable;
  }

  CtExpression<?> extractExprToReplace(CtExpression<?> nullExp) {
    return nullExp;
  }

  private boolean isLiteralNull(CtExpression nullExp) {
    return nullExp.getType().toString().equals(CtTypeReference.NULL_TYPE_NAME);
  }

  @Override
  List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    CtTypeReference<?> nullExpTyp = !isLiteralNull(nullExp) ? nullExp.getType()
        : nullExp.getParent(CtLocalVariable.class).getReference().getType();

    List<CtExpression<?>> typeCompatibleExprs = initializer.getTypeCompatibleExpressions(nullExp, nullExpTyp);
    if (nullExp.getParent() instanceof CtTargetedExpression) {
      List<CtExpression> replaceableExprs = initializer.getReplaceableExpressions(nullExp);
      return Stream.concat(typeCompatibleExprs.stream(), replaceableExprs.stream()).collect(Collectors.toList());
    }

    return typeCompatibleExprs.stream().collect(Collectors.toList());
  }
}