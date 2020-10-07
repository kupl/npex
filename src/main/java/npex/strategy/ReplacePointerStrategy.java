package npex.strategy;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javassist.CtConstructor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtRHSReceiver;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;

public class ReplacePointerStrategy extends AbstractReplaceStrategy {
  public ReplacePointerStrategy(ValueInitializer initializer) {
    super(initializer);
    this.name = "ReplacePointer" + initializer.getName();
  }

  CtExpression<?> extractExprToReplace(CtExpression<?> nullExp) {
    return nullExp;
  }

  private boolean isLiteralNull(CtExpression nullExp) {
    return nullExp.getType().toString().equals(CtTypeReference.NULL_TYPE_NAME);
  }

  private CtTypeReference getNullValueType(CtExpression nullExp) {
    CtElement parent = nullExp.getParent();
    if (parent instanceof CtAssignment) {
      return ((CtAssignment) parent).getAssigned().getType();
    } else if (parent instanceof CtLocalVariable) {
      return ((CtLocalVariable) parent).getReference().getType();
    } else if (nullExp.getRoleInParent().equals(CtRole.ARGUMENT)) {
      CtInvocation invo = nullExp.getParent(CtInvocation.class);
      return (CtTypeReference<?>) invo.getActualTypeArguments().get(invo.getArguments().indexOf(nullExp));
    } else {
      throw new IllegalArgumentException("Not supported null assignment form");
    }
  }

  @Override
  List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    CtTypeReference<?> nullExpTyp = !isLiteralNull(nullExp) ? nullExp.getType() : getNullValueType(nullExp);

    List<CtExpression<?>> typeCompatibleExprs = initializer.getTypeCompatibleExpressions(nullExp, nullExpTyp);
    if (nullExp.getParent() instanceof CtTargetedExpression && !(nullExp.getParent() instanceof CtConstructorCall)) {
      List<CtExpression> replaceableExprs = initializer.getReplaceableExpressions(nullExp);
      return Stream.concat(typeCompatibleExprs.stream(), replaceableExprs.stream()).collect(Collectors.toList());
    }

    return typeCompatibleExprs.stream().collect(Collectors.toList());
  }
}