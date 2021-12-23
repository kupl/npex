package npex.synthesizer.strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import npex.common.filters.ClassOrInterfaceFilter;
import npex.common.filters.MethodOrConstructorFilter;
import npex.common.utils.FactoryUtils;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtConstructorCall;
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
  private Set<CtClass<? extends Throwable>> collectThrownTypes(CtElement el) {
    Set<CtClass<? extends Throwable>> throwns = new HashSet<>();
    for (CtThrow thr : el.getElements(new TypeFilter<>(CtThrow.class))) {
      throwns.add((CtClass) thr.getThrownExpression().getType().getTypeDeclaration());
    }

    for (CtCatchVariable cv : el.getElements(new TypeFilter<>(CtCatchVariable.class))) {
      throwns.add((CtClass) cv.getType().getTypeDeclaration());
    }

    throwns.removeIf(s -> s == null);

    return throwns;
  }

  private Set<CtClass<? extends Throwable>> collectCandidateExceptionsTypes(CtExecutable exec) {
    Set<CtClass<? extends Throwable>> thrownsInSinkMethod = collectThrownTypes(exec);
    Set<CtClass<? extends Throwable>> thrownsInOtherMethods = collectThrownTypes(
        exec.getParent(new ClassOrInterfaceFilter()));
    Set<CtTypeReference> thrownTypes = exec.getThrownTypes();
    Set<CtClass<? extends Throwable>> thrownsByThrows = new HashSet<>();
    thrownTypes.stream().filter(ty -> ty != null).forEach(ty -> thrownsByThrows.add((CtClass) ty.getTypeDeclaration()));

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
    Set<CtClass<? extends Throwable>> exnTypes = collectCandidateExceptionsTypes(enclosingExecutable);
    List<CtStatement> throwStmts = new ArrayList<>();
    Factory factory = nullExp.getFactory();
    exnTypes.forEach(klass -> {
      CtThrow thw = factory.createThrow();
      CtConstructorCall call = null;
      if (klass.getConstructor() != null) {
        call = factory.createConstructorCall(klass.getReference());
      } else if (klass.getConstructor(new spoon.reflect.factory.TypeFactory().STRING) != null) {
        call = factory.createConstructorCall(klass.getReference(), FactoryUtils.createEmptyString());
      }
      if (call != null) {
        throwStmts.add(thw.setThrownExpression(call));
      }
    });
    return throwStmts;
  }

  protected boolean _isApplicable(CtExpression<?> nullExp) {
    return true;
  }

}