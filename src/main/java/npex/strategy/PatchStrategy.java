package npex.strategy;

import npex.template.PatchTemplate;
import spoon.reflect.code.CtExpression;

public interface PatchStrategy {
    String getName();

    boolean isApplicable(CtExpression<?> nullExp);

    PatchTemplate generate(CtExpression<?> nullExp);
}