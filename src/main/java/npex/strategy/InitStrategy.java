package npex.strategy;

import java.util.List;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;

public interface InitStrategy extends PatchStrategy {
  List<CtExpression<?>> getExpressionsToReplace(CtExpression<?> nullExp);

  CtStatement createNullBlockStmt(CtExpression<?> nullExp);
}