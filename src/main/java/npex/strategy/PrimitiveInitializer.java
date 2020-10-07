package npex.strategy;

import java.util.stream.Stream;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.reference.CtTypeReference;

public class PrimitiveInitializer extends ValueInitializer<CtLiteral> {
  public String getName() {
    return "Literal";
  }

  protected Stream<CtLiteral> enumerate(CtExpression expr) {
    CtTypeReference typ = expr.getType();
    if (!typ.isPrimitive())
      return Stream.empty();

    DefaultValueTable.getDefaultValues(typ).forEach(t -> System.out.println(t));

    return DefaultValueTable.getDefaultValues(typ).stream();
  }
}