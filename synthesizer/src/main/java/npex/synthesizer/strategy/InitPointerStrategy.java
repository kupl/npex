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

import npex.synthesizer.initializer.ValueInitializer;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThisAccess;
import spoon.reflect.code.CtVariableAccess;

@SuppressWarnings("rawtypes")
public class InitPointerStrategy extends AbstractSkipStrategy {
  final protected ValueInitializer initializer;

  public InitPointerStrategy(ValueInitializer initializer) {
    this.initializer = initializer;
  }

  public String getName() {
    return this.getClass().getName() + initializer.getName();
  }

  @SuppressWarnings("unchecked")
  protected List<CtStatement> createNullExecStatements(CtExpression nullExp) {
    List<CtExpression> values = initializer.getTypeCompatibleExpressions(nullExp, nullExp.getType());
    return values.stream().map(v -> {
      CtAssignment assignment = nullExp.getFactory().createAssignment();
      assignment.setAssigned(nullExp.clone());
      assignment.setAssigned(v);
      return assignment;
    }).collect(Collectors.toList());
  }

  @Override
  protected CtStatement createSkipTo(CtExpression nullExp) {
    return null;
  }

  protected boolean _isApplicable(CtExpression nullExp) {
    return nullExp instanceof CtVariableAccess || nullExp instanceof CtThisAccess;
  }
}
