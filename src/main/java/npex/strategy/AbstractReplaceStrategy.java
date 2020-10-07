package npex.strategy;

import java.util.List;
import java.util.stream.Collectors;

import npex.Utils;
import npex.template.PatchTemplate;
import npex.template.PatchTemplateTernary;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

abstract public class AbstractReplaceStrategy extends AbstractStrategy {
  final protected ValueInitializer initializer;

  public AbstractReplaceStrategy(ValueInitializer initializer) {
    this.initializer = initializer;
  }

  public boolean isApplicable(CtExpression nullExp) {
    if (extractExprToReplace(nullExp).getType().toString().equals("void"))
      return false;

    if (extractExprToReplace(nullExp).equals(Utils.getNearestSkippableStatement(nullExp))) {
      return false;
    }
    return true;
  }

  @Override
  protected CtExpression<?> createSkipFrom(CtExpression<?> nullExp) {
    return extractExprToReplace(nullExp);
  }

  @Override
  protected CtExpression<?> createSkipTo(CtExpression<?> nullExp) {
    return extractExprToReplace(nullExp);
  }

  @Override
  public List<PatchTemplate> generate(CtExpression<?> nullExp) {
    final CtExpression<?> skipFrom = (CtExpression<?>) this.createSkipFrom(nullExp);
    final CtExpression<?> skipTo = (CtExpression<?>) this.createSkipTo(nullExp);
    final List<CtElement> nullBlockStmt = createNullBlockStmts(nullExp);
    return nullBlockStmt.stream()
        .map(
            s -> new PatchTemplateTernary(getPatchID(skipFrom, skipTo), nullExp, (CtExpression<?>) s, skipFrom, skipTo))
        .collect(Collectors.toList());
  }

  abstract CtExpression<?> extractExprToReplace(CtExpression<?> nullExp);
}