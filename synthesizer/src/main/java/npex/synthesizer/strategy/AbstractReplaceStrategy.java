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

import npex.synthesizer.Utils;
import npex.synthesizer.template.PatchTemplate;
import npex.synthesizer.template.PatchTemplateTernary;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

abstract public class AbstractReplaceStrategy extends AbstractStrategy {
  final protected ValueInitializer initializer;

  public AbstractReplaceStrategy(ValueInitializer initializer) {
    this.initializer = initializer;
  }

  public boolean isApplicable(CtExpression nullExp) {
    if (extractExprToReplace(nullExp).getType().toString().equals("void"))
      return false;

    if (extractExprToReplace(nullExp).equals(Utils.getNearestSkippableStatement(nullExp))) {
      return false;
    }
    return true;
  }

  @Override
  protected CtExpression<?> createSkipFrom(CtExpression<?> nullExp) {
    return extractExprToReplace(nullExp);
  }

  @Override
  protected CtExpression<?> createSkipTo(CtExpression<?> nullExp) {
    return extractExprToReplace(nullExp);
  }

  @Override
  public List<PatchTemplate> generate(CtExpression<?> nullExp) {
    final CtExpression<?> skipFrom = (CtExpression<?>) this.createSkipFrom(nullExp);
    final CtExpression<?> skipTo = (CtExpression<?>) this.createSkipTo(nullExp);
    final List<CtElement> nullBlockStmt = createNullBlockStmts(nullExp);
    return nullBlockStmt.stream()
        .map(
            s -> new PatchTemplateTernary(getPatchID(skipFrom, skipTo), nullExp, (CtExpression<?>) s, skipFrom, skipTo))
        .collect(Collectors.toList());
  }

  abstract CtExpression<?> extractExprToReplace(CtExpression<?> nullExp);
}