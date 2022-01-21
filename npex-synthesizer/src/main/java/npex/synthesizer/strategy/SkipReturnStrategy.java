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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import npex.common.utils.FactoryUtils;
import npex.common.utils.TypeUtil;
import npex.synthesizer.enumerator.ExpressionEnumerator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtTypeReference;

public class SkipReturnStrategy extends AbstractSkipStrategy {

  @Override
  public boolean _isApplicable(CtExpression nullExp) {
    return true;
  }

  @Override
  protected List<CtStatement> createNullExecStatements(CtExpression nullExp) {
    spoon.reflect.factory.Factory factory = nullExp.getFactory();
    CtTypeReference retTyp = nullExp.getParent(CtConstructor.class) != null ? TypeUtil.VOID_PRIMITIVE
        : nullExp.getParent(CtMethod.class).getType();

    // In case that return type is missing which means that the type is a custom class type.
    if (retTyp == null) {
      CtReturn retStmt = factory.createReturn().setReturnedExpression(FactoryUtils.createNullLiteral());
      return (Collections.singletonList(retStmt));
    }

      // In case of void method, we just insert 'return;'
      if (retTyp.equals(TypeUtil.VOID_PRIMITIVE)) {
        CtReturn retStmt = factory.createReturn().setReturnedExpression(null);
        return Collections.singletonList(retStmt);
      }
      
      List<CtStatement> retStmts = new ArrayList<>();
      List<CtExpression> exprs = ExpressionEnumerator.enumTypeCompatibleExpressions(nullExp, retTyp);
      for (CtExpression e : exprs) {
      CtReturn retStmt = factory.createReturn().setReturnedExpression(e);
      retStmts.add(retStmt);
      }
    return retStmts;
  }
}