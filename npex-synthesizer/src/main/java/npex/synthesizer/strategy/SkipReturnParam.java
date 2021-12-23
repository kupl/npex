package npex.synthesizer.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import npex.common.filters.MethodOrConstructorFilter;
import npex.common.utils.FactoryUtils;
import npex.common.utils.TypeUtil;
import npex.synthesizer.initializer.DefaultValueTable;
import npex.synthesizer.initializer.ObjectInitializer;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtTypeReference;
import java.util.stream.Collectors;

public class SkipReturnParam extends SkipReturnStrategy {

  @Override
  protected CtStatement createSkipFrom(CtExpression nullExp) {
    return nullExp.getParent(new MethodOrConstructorFilter()).getBody().getStatements().get(0);
  }

  @Override
  public boolean _isApplicable(CtExpression nullExp) {
    return super._isApplicable(nullExp) && nullExp instanceof CtVariableAccess va
        && va.getVariable().getDeclaration() instanceof CtParameter;
  }

  @Override
  protected List<CtStatement> createNullExecStatements(CtExpression nullExp) {
    CtTypeReference retTyp = nullExp.getParent(CtConstructor.class) != null ? TypeUtil.VOID
        : nullExp.getParent(CtMethod.class).getType();

    // In case of void method, we just insert 'return;'
    if (retTyp.equals(TypeUtil.VOID)) {
      CtReturn retStmt = factory.createReturn().setReturnedExpression(null);
      return (Collections.singletonList(retStmt));
    }

    logger.info("retTyp: {}, {}", retTyp, retTyp.isPrimitive());
    if (retTyp.isPrimitive()) {
      List<CtStatement> retStmts = new ArrayList<>();
      for (CtLiteral l : (List<CtLiteral>) DefaultValueTable.getDefaultValues(retTyp)) {
        CtReturn retStmt = factory.createReturn().setReturnedExpression(l);
        retStmts.add(retStmt);
      }
      return retStmts;
    }

    List<CtExpression> values = ObjectInitializer.enumerate(retTyp).collect(Collectors.toList());
    values.add(FactoryUtils.createNullLiteral());
    logger.info("retTyp: {}, Values: {}", retTyp, values);
    List<CtStatement> retStmts = new ArrayList<>();
    for (CtExpression e : values) {
      CtReturn retStmt = factory.createReturn().setReturnedExpression(e);
      retStmts.add(retStmt);
    }
    return retStmts;
  }
}