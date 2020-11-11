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
import java.util.stream.Collectors;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;

@SuppressWarnings("rawtypes")
public class InitPointerStrategy extends AbstractStrategy {
  final protected ValueInitializer initializer;

  public InitPointerStrategy(ValueInitializer initializer) {
    this.initializer = initializer;
    this.name = "InitPointer" + initializer.getName();
  }

  public boolean isApplicable(CtExpression<?> nullExp) {
    return nullExp instanceof CtVariableAccess || nullExp instanceof CtThisAccess;
  }

  protected <T, A extends T> CtAssignment<T, A> createAssignment(CtExpression<T> nullExp, CtExpression<A> value) {
    CtAssignment<T, A> assignment = nullExp.getFactory().createAssignment();
    assignment.setAssigned(nullExp);
    assignment.setAssignment(value);
    return assignment;
  }

  @SuppressWarnings("unchecked")
  protected <T> List<CtElement> createInitializeStatements(CtExpression<T> nullExp) {
    List<CtExpression<? extends T>> values = initializer.getTypeCompatibleExpressions(nullExp, nullExp.getType());
    return values.stream().map(v -> createAssignment(nullExp.clone(), v)).collect(Collectors.toList());
  }

  @Override
  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return null;
  }

  List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    return createInitializeStatements(nullExp);
  }
}