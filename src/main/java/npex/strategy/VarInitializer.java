package npex.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class VarInitializer extends ValueInitializer {
  Logger logger = Logger.getLogger(VarInitializer.class);

  public String getName() {
    return "Var";
  }

  public <T> List<CtVariableAccess<? extends T>> getVarAccesses(CtMethod<?> method, CtExpression<T> nullExp) {
    List<CtStatement> bodyStmts = method.getBody().getStatements();
    CtTypeReference<T> typ = nullExp.getType();
    List<CtVariableAccess<? extends T>> variables = bodyStmts.stream()
        .flatMap(s -> s.getElements(new TypeFilter<CtVariableAccess<? extends T>>(CtVariableAccess.class)).stream())
        .filter(v -> v.getType().isSubtypeOf(typ)).distinct().filter(v -> !v.toString().equals(nullExp.toString()))
        .peek(x -> logger.info(
            String.format("-- VariableAccessFound: %s, Typ: %s, %s", x, x.getType(), x.getType().isSubtypeOf(typ))))
        .collect(Collectors.toList());
    return variables;
  }

  public <T> List<CtExpression<? extends T>> getInitializerExpressions(CtExpression<T> nullExp) {
    CtMethod<?> method = nullExp.getParent(CtMethod.class);
    try {
      List<CtVariableAccess<? extends T>> varAccesses = getVarAccesses(method, nullExp);
      return varAccesses.stream().collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("NullExprClass: " + nullExp.getParent(CtClass.class));
      return new ArrayList<>();
    }
  }
}