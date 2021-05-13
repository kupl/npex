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

import npex.common.utils.ASTUtils;
import npex.common.utils.FactoryUtils;
import npex.synthesizer.initializer.ValueInitializer;
import spoon.reflect.code.CtExpression;
import java.util.ArrayList;

public class ReplaceEntireExpressionStrategy extends AbstractReplaceStrategy {

  public ReplaceEntireExpressionStrategy(ValueInitializer initializer) {
    super(initializer);
  }

  public boolean isApplicable(CtExpression nullExp) {
    return !nullExp.toString().equals("null");
  }

  @Override
  protected List<CtExpression> extractExprToReplace(CtExpression nullExp) {
    return List.of(ASTUtils.getOutermostExpression(nullExp));
  }

  protected List<CtExpression> enumerateAvailableExpressions(CtExpression expr) {
    List<CtExpression> exprs = new ArrayList<>();
    for (CtExpression exprToRep : extractExprToReplace(expr)) {
      exprs.addAll(initializer.getTypeCompatibleExpressions(exprToRep, exprToRep.getType()));
      if (!exprToRep.getType().isPrimitive()) {
        exprs.add(FactoryUtils.createNullLiteral());
      }
    }
    return exprs;
  }
}