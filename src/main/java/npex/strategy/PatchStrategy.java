package npex.strategy;

import java.util.List;

import npex.template.PatchTemplate;
import spoon.reflect.code.CtExpression;

public interface PatchStrategy {
    String getName();

    boolean isApplicable(CtExpression<?> nullExp);

    List<PatchTemplate> generate(CtExpression<?> nullExp);
}