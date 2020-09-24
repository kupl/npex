package npex.strategy;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.reference.CtTypeReference;

@SuppressWarnings("rawtypes")
public abstract class ValueInitializer<T extends CtExpression> {
  Logger logger = Logger.getLogger(ValueInitializer.class);

  public abstract String getName();

  protected abstract Stream<T> enumerate(CtExpression expr);

  protected abstract Predicate<T> isAccessible(CtTypeReference typ);

  @SuppressWarnings("unchecked")
  public <S> List<CtExpression<? extends S>> getTypeCompatibleExpressions(CtExpression expr, CtTypeReference<S> typ) {
    return (List<CtExpression<? extends S>>) this.enumerate(expr, t -> t.getType().isSubtypeOf(typ));
  }

  public List<T> getReplaceableExpressions(CtExpression expr) throws IllegalArgumentException {
    if (!(expr.getParent() instanceof CtTargetedExpression)) {
      throw new IllegalArgumentException(String.format("Parent of %s should be a targeted expression", expr));
    }

    CtTypeReference targetedTyp = expr.getParent(CtTargetedExpression.class).getType();
    return enumerate(expr, isAccessible(targetedTyp));
  }

  private List<T> enumerate(CtExpression expr, Predicate<T> pred) {
    Stream<T> candidates = this.enumerate(expr).filter(c -> !c.equals(expr));
    return candidates.filter(pred).collect(Collectors.toList());
  }
}