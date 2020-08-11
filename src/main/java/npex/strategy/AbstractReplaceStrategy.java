package npex.strategy;

import org.apache.log4j.Logger;

import npex.Utils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
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

  CtStatement createNullBlockStmt(CtExpression<?> nullExp) {
    CtStatement sinkStmtClone = (CtStatement) this.createSkipFrom(nullExp).clone();
    CtExpression<?> exprToReplace = extractExprToReplace(nullExp);
    CtExpression<?> exprToReplaceClone = Utils.findMatchedElement(sinkStmtClone, exprToReplace);
    exprToReplaceClone.replace(initializer.getInitializerExpressions(exprToReplace).get(0).clone());
    return sinkStmtClone;
  }

  abstract public CtExpression<?> extractExprToReplace(CtExpression<?> nullExp);
}