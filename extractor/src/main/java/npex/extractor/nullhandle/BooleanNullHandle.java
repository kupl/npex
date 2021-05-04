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
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;

public class BooleanNullHandle extends AbstractNullHandle<CtBinaryOperator<Boolean>> {

  public BooleanNullHandle(CtBinaryOperator<Boolean> handle, CtBinaryOperator<Boolean> nullCond) {
    super(handle, nullCond);
  }

  @Override
  protected AbstractNullModelScanner createNullModelScanner(CtExpression nullExp) {
    return new NullModelScanner(nullExp);
  }

  private class NullModelScanner extends AbstractNullModelScanner {
    private CtExpression root;
    private final BinaryOperatorKind handleBoKind;

    public NullModelScanner(CtExpression nullExp) {
      super(nullExp);
      this.handleBoKind = handle.getKind();
    }

    @Override
    public void visitCtBinaryOperator(CtBinaryOperator bo) {
      root = bo;
      BinaryOperatorKind kind = bo.getKind();
      if (kind.equals(BinaryOperatorKind.OR) || kind.equals(BinaryOperatorKind.AND)) {
        // Exclude boolean handles with inconsistent bops, e.g., p != null && e1 || e2
        if (!kind.equals(handleBoKind)) {
          terminate();
        }
        root = bo.getRightHandOperand();
        if (root instanceof CtBinaryOperator boRHS) {
          visitCtBinaryOperator(boRHS);
        } else if (root instanceof CtInvocation invo && isTargetInvocation(invo)) {
          models.add(createModel(invo));
          terminate();
        } else {
          terminate();
        }
      } else {
        terminate();
      }
    }

    @Override
    protected NullModel createModel(CtInvocation invo) {
      /* TODO: resolve compound cases e.g.m !(e), e == false ... */
      CtExpression nullValue = factory.createLiteral().setValue(this.handleBoKind.equals(BinaryOperatorKind.OR));
      return new NullModel(nullExp, root, nullValue);
    }

  }
}