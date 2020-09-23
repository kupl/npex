package npex.strategy;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.reference.CtTypeReference;

public abstract class ValueInitializer<T extends CtExpression> {
  public abstract String getName();

  /*
   * Use this method to initialize expression with a specific type other than the
   * expression's type
   */
  protected abstract Stream<T> enumerate(CtExpression expr);

  private List<T> enumerate(CtExpression expr, Predicate<T> pred) {
    return this.enumerate(expr).filter(pred).collect(Collectors.toList());
  }

  public List<T> getInitializerExpressions(CtExpression expr) {
    return this.enumerate(expr, t -> !t.equals(expr));
  }

  @SuppressWarnings("unchecked")
  public <S> List<CtExpression<? extends S>> getTypeCompatibleExpressions(CtExpression expr, CtTypeReference<S> typ) {
    return (List<CtExpression<? extends S>>) this.enumerate(expr, t -> t.getType().isSubtypeOf(typ));
  }

  //TODO
  public List<T> getReplaceableExpressions(CtExpression expr) {
    if (expr instanceof CtTargetedExpression) {
      return null;
    }
    return null;
  }
}