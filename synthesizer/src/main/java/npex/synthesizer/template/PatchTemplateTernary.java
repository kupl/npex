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
package npex.synthesizer.template;

import npex.synthesizer.Utils;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtExecutable;

public class PatchTemplateTernary extends PatchTemplate {
  private final CtExpression exprToReplace;
  private final CtExpression alternativeValue;

  public PatchTemplateTernary(String id, CtExpression nullExp, CtExpression exprToReplace,
      CtExpression alternativeValue) {
    super(id, nullExp);
    this.exprToReplace = Utils.findMatchedElementLookParent(ast, exprToReplace);
    this.alternativeValue = alternativeValue;
  }

  protected CtExecutable implement() {
    CtConditional ternary = factory.createConditional();
    ternary.setCondition(createNullCond(false));
    ternary.setThenExpression(exprToReplace.clone());
    ternary.setElseExpression(alternativeValue);

    exprToReplace.replace(ternary);
    return ast;
  }
}