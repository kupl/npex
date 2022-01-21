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
package npex.synthesizer.initializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.utils.FactoryUtils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtActualTypeContainer;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

@SuppressWarnings("rawtypes")
public abstract class ValueInitializer<T extends CtTypedElement> {
  final static Logger logger = LoggerFactory.getLogger(ValueInitializer.class);
  final static TypeFactory tf = new TypeFactory();

  public abstract String getName();

  protected abstract Stream<T> enumerate(CtExpression expr);

  protected abstract CtExpression convertToCtExpression(T typedElement);

  public List<CtExpression> getTypeCompatibleExpressions(CtExpression expr, CtTypeReference typ) {
    if (typ == null) {
      return Collections.emptyList();
    }

    /* We remove the type arguments in a generic type because it disturbs subtpye checking.
    Simply ignore them and leave it to compiler */
    final CtTypeReference _typ;
    if (typ instanceof CtActualTypeContainer container) {
      _typ = typ.clone();
      _typ.setActualTypeArguments(new ArrayList<>());
    } else {
      _typ = typ;
    }

    Predicate<T> filter = cand -> {
      try {
        return cand.getType().toString().endsWith("<>") || cand.getType().isSubtypeOf(_typ);
      } catch (Exception e) {
        return false;
      }
    };
    return this.enumerate(expr, filter);
  }

  public List<CtExpression> getReplaceableExpressions(CtExpression expr) throws IllegalArgumentException {
    if (!(expr.getParent() instanceof CtTargetedExpression)) {
      throw new IllegalArgumentException(String.format("Parent of %s should be a targeted expression", expr));
    }

    CtTargetedExpression targetedExpression = expr.getParent(CtTargetedExpression.class);
    Predicate<T> filter = ty -> isTargetedExpressionAccessible(ty, targetedExpression);
    return this.enumerate(expr, filter);
  }

  private List<CtExpression> enumerate(CtExpression expr, Predicate<T> pred) {
    List<CtExpression> lst = new ArrayList<>();
    lst.add(FactoryUtils.createNullLiteral());
    if (expr.getType() == null) {
      logger.error("Could not find type of {}", expr);
      return lst;
    }
    Stream<T> candidates = this.enumerate(expr).filter(c -> !c.equals(expr));
    lst.addAll(candidates.filter(pred).map(c -> convertToCtExpression(c)).collect(Collectors.toList()));
    return lst;
  }

  private boolean isTargetedExpressionAccessible(T candidate, CtTargetedExpression target) {
    if (target instanceof CtFieldAccess fa) {
      CtField field = fa.getVariable().getFieldDeclaration();
      if (field == null) {
        logger.error("Cannot find field declaration for {}", fa.getVariable());
        return false;
      }
      CtFieldReference candFieldRef = candidate.getType().getDeclaredOrInheritedField(field.getSimpleName());

      if (candFieldRef == null) {
        return false;
      }

      CtField candField = candFieldRef.getDeclaration();
      return candField.getSimpleName().equals(field.getSimpleName()) && candField.getType().equals(field.getType());
    }

    if (target instanceof CtInvocation) {
      String signature = ((CtInvocation) target).getExecutable().getSignature();
      CtTypeReference candType = candidate.getType();
      if (candType == null) {
        logger.error("Cannot find type of candidate {}", candType);
        return false;
      }
      return candType.getAllExecutables().stream()
          .anyMatch(e -> e.getSignature().equals(signature) && e.getType().equals(target.getType()));
    }

    throw new IllegalArgumentException();
  }
}