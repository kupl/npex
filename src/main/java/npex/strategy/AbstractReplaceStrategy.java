package npex.strategy;

import org.apache.log4j.Logger;

import npex.template.PatchTemplate;
import npex.template.PatchTemplateTernary;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;

abstract public class AbstractReplaceStrategy extends AbstractStrategy {
  final protected ValueInitializer initializer;
  final protected Logger logger = Logger.getLogger(AbstractStrategy.class);

  public AbstractReplaceStrategy(ValueInitializer initializer) {
    this.initializer = initializer;
  }

  @Override
  public boolean isApplicable(CtExpression<?> nullExp) {
    if (nullExp.getRoleInParent().equals(CtRole.ARGUMENT)) {
      return false;
    }

    CtExpression<?> exprToReplace = extractExprToReplace(nullExp);
    try {
      return !initializer.getInitializerExpressions(exprToReplace).isEmpty();
    } catch (Exception e) {
      logger.fatal(String.format("Cannot initialize expression %s with other values", exprToReplace, e));
      return false;
    }
  }

  @Override
  protected CtExpression<?> createSkipFrom(CtExpression<?> nullExp) {
    return extractExprToReplace(nullExp);
  }

  @Override
  protected CtExpression<?> createSkipTo(CtExpression<?> nullExp) {
    return extractExprToReplace(nullExp);
  }

  CtElement createNullBlockStmt(CtExpression<?> nullExp) {
    CtExpression<?> exprToReplace = extractExprToReplace(nullExp);
    CtExpression<?> value = initializer.getInitializerExpressions(exprToReplace).get(0).clone();
    return value;
  }

  @Override
  public PatchTemplate generate(CtExpression<?> nullExp) {
    final CtExpression<?> skipFrom = (CtExpression<?>) this.createSkipFrom(nullExp);
    final CtExpression<?> skipTo = (CtExpression<?>) this.createSkipTo(nullExp);
    final CtExpression<?> nullBlockStmt = (CtExpression<?>) createNullBlockStmt(nullExp);
    final String patchID = this.getPatchID(skipFrom, skipTo);
    return new PatchTemplateTernary(patchID, nullExp, nullBlockStmt, skipFrom, skipTo);
  }

  abstract public CtExpression<?> extractExprToReplace(CtExpression<?> nullExp);
}