package npex.synthesizer.strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import npex.common.filters.ClassOrInterfaceFilter;
import npex.common.filters.MethodOrConstructorFilter;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;

public class SkipThrowStrategy extends AbstractSkipStrategy {
  private Set<CtTypeReference<? extends Throwable>> collectThrownTypes(CtElement el) {
    Set<CtTypeReference<? extends Throwable>> throwns = new HashSet<>();
    for (CtThrow thr : el.getElements(new TypeFilter<>(CtThrow.class))) {
      throwns.add(thr.getThrownExpression().getType());
    }
    return throwns;
  }

  private Set<CtTypeReference<? extends Throwable>> collectCandidateExceptionsTypes(CtExecutable exec) {
    var thrownsInSinkMethod = collectThrownTypes(exec);
    var thrownsInOtherMethods = collectThrownTypes(exec.getParent(new ClassOrInterfaceFilter()));
    var thrownsByThrows = exec.getThrownTypes();

    List<Set> nonEmptyThrownSets = new ArrayList<Set>();
    nonEmptyThrownSets.add(thrownsInSinkMethod);
    nonEmptyThrownSets.add(thrownsInOtherMethods);
    nonEmptyThrownSets.add(thrownsByThrows);
    nonEmptyThrownSets.removeIf(s -> s.isEmpty());

    if (nonEmptyThrownSets.isEmpty()) {
      return new HashSet<>();
    }

    var exnTypes = nonEmptyThrownSets.get(0);
    nonEmptyThrownSets.forEach(s -> exnTypes.retainAll(s));
    return exnTypes;
  }

  protected List<CtStatement> createNullExecStatements(CtExpression nullExp) {
    CtExecutable enclosingExecutable = nullExp.getParent(new MethodOrConstructorFilter());
    Set<CtTypeReference<? extends Throwable>> exnTypes = collectCandidateExceptionsTypes(enclosingExecutable);
    List<CtStatement> throwStmts = new ArrayList<>();
    Factory factory = nullExp.getFactory();
    exnTypes.forEach(ty -> {
      CtThrow thw = factory.createThrow().setThrownExpression(factory.createConstructorCall(ty));
      throwStmts.add(thw);
    });
    return throwStmts;
  }

  protected boolean _isApplicable(CtExpression<?> nullExp) {
    return true;
  }

}