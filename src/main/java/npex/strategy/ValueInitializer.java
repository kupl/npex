package npex.strategy;

import java.util.List;

import spoon.reflect.code.CtExpression;

public abstract class ValueInitializer {

  public abstract String getName();

  public abstract <T> List<CtExpression<? extends T>> getInitializerExpressions(CtExpression<T> expr);
}