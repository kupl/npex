package npex.strategy;

import java.util.List;
import java.util.stream.Collectors;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

public class SkipReturnStrategy extends SkipStrategy {
  public SkipReturnStrategy() {
    this.name = "SkipReturn";
  }

  @Override
  public boolean _isApplicable(CtExpression<?> nullExp) {
    CtMethod<?> sinkMethod = nullExp.getParent(CtMethod.class);
    /* sink in the constructor */
    if (sinkMethod == null) {
      return false;
    }

    CtTypeReference<?> typ = sinkMethod.getType();
    if (DefaultValueTable.hasDefaultValue(typ)) {
      logger.info("Return typ is: " + typ);
    }
    return DefaultValueTable.hasDefaultValue(typ);
  }

  @Override
  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return nullExp.getParent(CtMethod.class);
  }

  protected <R> List<CtReturn<R>> createReturnStmts(CtMethod<R> sinkMethod) {
    Factory factory = sinkMethod.getFactory();
    final CtTypeReference<R> retTyp = sinkMethod.getType();

    return DefaultValueTable.getDefaultValues(retTyp).stream().map(e -> {
      CtReturn<R> retStmt = factory.createReturn();
      retStmt.setReturnedExpression(e);
      return retStmt;
    }).collect(Collectors.toList());
  }

  @Override
  protected List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    return (List<CtElement>) createReturnStmts(nullExp.getParent(CtMethod.class));
  }
}