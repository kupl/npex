package npex.strategy;

import java.util.List;

import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

public abstract class ValueInitializer {
  public abstract String getName();

  /*
   * Use this method to initialize expression with a specific type other than the
   * expression's type
   */
  public abstract <T> List<CtExpression<? extends T>> getInitializerExpressions(CtExpression<?> expr,
      CtTypeReference<T> typ);

  public <T> List<CtExpression<? extends T>> getInitializerExpressions(CtExpression<T> expr) {
    return getInitializerExpressions(expr, expr.getType());
  }
}