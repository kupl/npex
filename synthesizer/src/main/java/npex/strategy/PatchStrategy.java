package npex.synthesizer.strategy;

import java.util.List;

import npex.synthesizer.template.PatchTemplate;
import spoon.reflect.code.CtExpression;

public interface PatchStrategy {
    String getName();

    boolean isApplicable(CtExpression<?> nullExp);

    List<PatchTemplate> generate(CtExpression<?> nullExp);
}