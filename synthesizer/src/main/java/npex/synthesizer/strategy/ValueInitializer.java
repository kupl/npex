/**
 * The MIT License
 *
 * Copyright (c) 2020 Software Analysis Laboratory, Korea University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package npex.synthesizer.strategy;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

@SuppressWarnings("rawtypes")
public abstract class ValueInitializer<T extends CtTypedElement> {
  Logger logger = Logger.getLogger(ValueInitializer.class);

  public abstract String getName();

  protected abstract Stream<T> enumerate(CtExpression expr);

  protected abstract CtExpression convertToCtExpression(T typedElement);

  @SuppressWarnings("unchecked")
  public <S> List<CtExpression<? extends S>> getTypeCompatibleExpressions(CtExpression expr, CtTypeReference<S> typ) {

    Predicate<T> filter = ty -> {
      try {
        return ty.getType().isSubtypeOf(typ);
      } catch (Exception e) {
        return false;
      }
    };
    return (List<CtExpression<? extends S>>) this.enumerate(expr, filter);
  }

  public List<T> getReplaceableExpressions(CtExpression expr) throws IllegalArgumentException {
    if (!(expr.getParent() instanceof CtTargetedExpression)) {
      throw new IllegalArgumentException(String.format("Parent of %s should be a targeted expression", expr));
    }

    CtTargetedExpression targetedExpression = expr.getParent(CtTargetedExpression.class);
    return this.enumerate(expr).filter(c -> isTargetedExpressionAccessible(c, targetedExpression))
        .collect(Collectors.toList());
  }

  private List<T> enumerate(CtExpression expr, Predicate<T> pred) {
    Stream<T> candidates = this.enumerate(expr).filter(c -> !c.equals(expr));
    return candidates.filter(pred).map(c -> convertToCtExpression(c)).collect(Collectors.toList());
  }

  private boolean isTargetedExpressionAccessible(T candidate, CtTargetedExpression target) {
    if (target instanceof CtFieldAccess) {
      CtField field = ((CtFieldAccess) target).getVariable().getFieldDeclaration();
      CtFieldReference candFieldRef = candidate.getType().getDeclaredOrInheritedField(field.getSimpleName());
      if (candFieldRef == null) {
        return false;
      }

      CtField candField = candFieldRef.getDeclaration();
      return candField.getSimpleName().equals(field.getSimpleName()) && candField.getType().equals(field.getType());
    }

    if (target instanceof CtInvocation) {
      String signature = ((CtInvocation) target).getExecutable().getSignature();
      System.out.println("Candidate: " + candidate);
      System.out.println("Target: " + target);
      System.out.println("cand type: " + candidate.getType());
      System.out.println("cand parent: " + candidate.getParent(CtMethod.class));
      System.out.println("cand parent (Constructor): " + candidate.getParent(CtConstructor.class));

      candidate.getType();
      candidate.getType().getAllExecutables();
      return candidate.getType().getAllExecutables().stream()
          .anyMatch(e -> e.getSignature().equals(signature) && e.getType().equals(target.getType()));
    }

    throw new IllegalArgumentException();
  }
}