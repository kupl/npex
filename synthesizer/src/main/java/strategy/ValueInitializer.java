package npex.synthesizer.strategy;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

@SuppressWarnings("rawtypes")
public abstract class ValueInitializer<T extends CtTypedElement> {
  Logger logger = Logger.getLogger(ValueInitializer.class);

  public abstract String getName();

  protected abstract Stream<T> enumerate(CtExpression expr);

  @SuppressWarnings("unchecked")
  public <S> List<CtExpression<? extends S>> getTypeCompatibleExpressions(CtExpression expr, CtTypeReference<S> typ) {
    Predicate<T> filter = ty -> {
      try {
        return ty.getType().isSubtypeOf(typ);
      } catch (Exception e) {
        return false;
      }
    };
    return (List<CtExpression<? extends S>>) this.enumerate(expr, filter);
  }

  public List<T> getReplaceableExpressions(CtExpression expr) throws IllegalArgumentException {
    if (!(expr.getParent() instanceof CtTargetedExpression)) {
      throw new IllegalArgumentException(String.format("Parent of %s should be a targeted expression", expr));
    }

    CtTargetedExpression targetedExpression = expr.getParent(CtTargetedExpression.class);
    return this.enumerate(expr).filter(c -> isTargetedExpressionAccessible(c, targetedExpression))
        .collect(Collectors.toList());
  }

  private List<T> enumerate(CtExpression expr, Predicate<T> pred) {
    Stream<T> candidates = this.enumerate(expr).filter(c -> !c.equals(expr));
    return candidates.filter(pred).collect(Collectors.toList());
  }

  private boolean isTargetedExpressionAccessible(T candidate, CtTargetedExpression target) {
    if (target instanceof CtFieldAccess) {
      CtField field = ((CtFieldAccess) target).getVariable().getFieldDeclaration();
      CtFieldReference candFieldRef = candidate.getType().getDeclaredOrInheritedField(field.getSimpleName());
      if (candFieldRef == null) {
        return false;
      }

      CtField candField = candFieldRef.getDeclaration();
      return candField.getSimpleName().equals(field.getSimpleName()) && candField.getType().equals(field.getType());
    }

    if (target instanceof CtInvocation) {
      String signature = ((CtInvocation) target).getExecutable().getSignature();
      return candidate.getType().getAllExecutables().stream()
          .anyMatch(e -> e.getSignature().equals(signature) && e.getType().equals(target.getType()));
    }

    throw new IllegalArgumentException();
  }
}