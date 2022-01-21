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
package npex.extractor.nullhandle;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;

public class TernaryNullHandle extends AbstractNullHandle<CtConditional> {
  public TernaryNullHandle(CtConditional ternary, CtBinaryOperator cond) {
    super(ternary, cond);
  }

  @Override
  protected AbstractNullModelScanner createNullModelScanner(CtExpression nullExp) {
    return new NullModelScanner(nullExp, nullCond.getKind().equals(BinaryOperatorKind.EQ));
  }

  private class NullModelScanner extends AbstractNullModelScanner {
    final private CtExpression sinkExpr;

    public NullModelScanner(CtExpression nullExp, boolean isCondKindEQ) {
      super(nullExp);
      this.sinkExpr = isCondKindEQ ? handle.getElseExpression() : handle.getThenExpression();
      if (sinkExpr instanceof CtAbstractInvocation invo && isTargetInvocation(invo)) {
        NullValue nullValue = NullValue.fromExpression(invo, isCondKindEQ ? handle.getThenExpression() : handle.getElseExpression());
        models.add(new NullModel(nullExp, sinkExpr, nullValue));
      } else if (sinkExpr instanceof CtUnaryOperator un && un.getKind().equals(UnaryOperatorKind.NOT)
          && isTargetInvocation(un.getOperand())) {
        NullValue nullValue = NullValue.fromExpression((CtAbstractInvocation) un.getOperand(), isCondKindEQ ? handle.getThenExpression() : handle.getElseExpression());
        if (nullValue.negate() != null) {
          models.add(new NullModel(nullExp, sinkExpr, nullValue));
        }
      }
    }

    @Override
    public void visitCtConditional(CtConditional ternary) {
      terminate();
    }

  }
}