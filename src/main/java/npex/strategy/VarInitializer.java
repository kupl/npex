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

  <T> List<CtVariableAccess<? extends T>> getVarAccesses(CtMethod<?> method, CtTypeReference<T> typ) {
    List<CtStatement> bodyStmts = method.getBody().getStatements();
    List<CtVariableAccess<? extends T>> variables = bodyStmts.stream()
        .flatMap(s -> s.getElements(new TypeFilter<CtVariableAccess<? extends T>>(CtVariableAccess.class)).stream())
        .filter(v -> v.getType().isSubtypeOf(typ)).distinct().collect(Collectors.toList());
    return variables;
  }

  public <T> List<CtExpression<? extends T>> getInitializerExpressions(CtExpression<?> nullExp,
      CtTypeReference<T> typ) {
    CtMethod<?> method = nullExp.getParent(CtMethod.class);
    try {
      List<CtVariableAccess<? extends T>> varAccesses = getVarAccesses(method, typ);
      varAccesses.remove(nullExp);
      return varAccesses.stream().collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("NullExprClass: " + nullExp.getParent(CtClass.class));
      return new ArrayList<>();
    }
  }
}