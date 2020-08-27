package npex.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

  List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    CtExpression<?> exprToReplace = extractExprToReplace(nullExp);
    ArrayList<CtElement> l = new ArrayList<>();
    for (CtExpression<?> value : initializer.getInitializerExpressions(exprToReplace)) {
      l.add(value);
    }
    return l;
  }

  @Override
  public List<PatchTemplate> generate(CtExpression<?> nullExp) {
    final CtExpression<?> skipFrom = (CtExpression<?>) this.createSkipFrom(nullExp);
    final CtExpression<?> skipTo = (CtExpression<?>) this.createSkipTo(nullExp);
    final String patchID = this.getPatchID(skipFrom, skipTo);
    final List<CtElement> nullBlockStmt = createNullBlockStmts(nullExp);
    return nullBlockStmt.stream()
        .map(s -> new PatchTemplateTernary(patchID, nullExp, (CtExpression<?>) s, skipFrom, skipTo))
        .collect(Collectors.toList());
  }

  abstract public CtExpression<?> extractExprToReplace(CtExpression<?> nullExp);
}