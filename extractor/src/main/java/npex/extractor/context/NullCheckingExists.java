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
package npex.extractor.context;

import npex.common.filters.MethodOrConstructorFilter;
import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.visitor.EarlyTerminatingScanner;

public class NullCheckingExists implements Context {
  public Boolean extract(CtInvocation invo, int nullPos) {
    CtExecutable exec = invo.getParent(new MethodOrConstructorFilter());
    if (exec == null) {
      return false;
    }
    CtExpression nullExp = nullPos == -1 ? invo.getTarget() : (CtExpression) invo.getArguments().get(nullPos);
    ContextScanner scanner = new ContextScanner(invo, nullExp);
    exec.accept(scanner);
    return scanner.getResult();
  }

  private class ContextScanner extends EarlyTerminatingScanner<Boolean> {
    final private CtExpression nullExp;

    public ContextScanner(CtInvocation invo, CtExpression nullExp) {
      this.nullExp = nullExp;
      setResult(false);
    }

    public void visitCtBinaryOperator(CtBinaryOperator cond) {
      super.visitCtBinaryOperator(cond);
      if (ASTUtils.isNullCondition(cond) && cond.getLeftHandOperand().equals(nullExp)) {
        setResult(true);
        terminate();
      }
    }
  }

}