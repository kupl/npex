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
package npex.common.utils;

import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.filters.EqualsFilter;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtRHSReceiver;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.code.CtWhile;
import spoon.reflect.cu.position.NoSourcePosition;
import spoon.reflect.declaration.CtElement;

public class ASTUtils {
  final static Logger logger = LoggerFactory.getLogger(ASTUtils.class);

  public static CtStatement getNearestSkippableStatement(CtElement el) {
    if (el instanceof CtStatement && el.getParent() instanceof CtBlock)
      return (CtStatement) el;

    CtStatement parent;
    while ((parent = el.getParent(CtStatement.class)) != null) {
      if (parent.getParent() instanceof CtBlock) {
        return (CtStatement) parent;
      }
      el = parent;
    }

    throw new IllegalArgumentException("this should not happen");
  }

  public static CtExpression getOutermostExpression(CtExpression e) {
    while (e.getParent() instanceof CtExpression && !(e.getParent() instanceof CtBinaryOperator)) {
      CtExpression parent = (CtExpression) e.getParent();
      if (parent instanceof CtVariableWrite || parent instanceof CtRHSReceiver || parent instanceof CtLambda) {
        break;
      }
      e = parent;
    }
    return e;
  }

  public static CtStatement getEnclosingStatement(CtElement el) {
    while (el != null) {
      CtStatement parent = el.getParent(CtStatement.class);
      if (parent == null) {
        return (CtStatement) el;
      }
      if (parent.getPosition() instanceof NoSourcePosition) {
        return (CtStatement) el;
      }
      if (parent.getPosition().getLine() != el.getPosition().getLine() || parent instanceof CtBlock)
        return (CtStatement) el;
      el = parent;
    }
    throw new IllegalArgumentException("this should not happen");
  }

  private static Stream<CtElement> getMatchedElements(CtElement at, CtElement element, Predicate<CtElement> pred) {
    return StreamSupport.stream(at.asIterable().spliterator(), false)
        .filter(x -> x.equals(element) && pred.test(x) && x.getPosition().getLine() == element.getPosition().getLine());
  }

  public static <T extends CtElement> T findMatchedElement(CtElement at, CtElement element)
      throws NoSuchElementException {
    return (T) getMatchedElements(at, element, x -> true).findFirst().get();
  }

  public static <T extends CtElement> T findMatchedElementLookParent(CtElement at, CtElement element)
      throws NoSuchElementException {
    try {
      return (T) getMatchedElements(at, element, x -> x.getParent().equals(element.getParent())).findFirst().get();
    } catch (NoSuchElementException e) {
      logger.error("Could not find {} in {}", element, at);
      throw e;
    }

  }

  public static CtElement getLoopHeadElement(CtForEach loop) {
    return loop.getExpression();
  }

  public static CtElement getLoopHeadElement(CtWhile loop) {
    return loop.getLoopingExpression();
  }

  public static CtElement getLoopHeadElement(CtLoop loop) {
    if (loop instanceof CtForEach) {
      return getLoopHeadElement((CtForEach) loop);
    }
    if (loop instanceof CtWhile) {
      return getLoopHeadElement((CtWhile) loop);
    }
    return null;
  }

  public static boolean isNullCondition(CtBinaryOperator bo) {
    return bo.getRightHandOperand()instanceof CtLiteral lit && lit.toString().equals("null");
  }

  public static boolean isChildOf(CtElement el, CtElement root) {
    return !root.filterChildren(new EqualsFilter(el)).list().isEmpty();
  }
}