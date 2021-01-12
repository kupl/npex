package npex.synthesizer.strategy;

import java.util.List;

import npex.synthesizer.template.PatchTemplate;
import spoon.reflect.code.CtExpression;

public interface PatchStrategy<T extends PatchTemplate> {

  public default String getName() {
    return this.getClass().getSimpleName();
  }

  boolean isApplicable(CtExpression<?> nullExp);

  List<T> enumerate(CtExpression<?> nullExp);
}