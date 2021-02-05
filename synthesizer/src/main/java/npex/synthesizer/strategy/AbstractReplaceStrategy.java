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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.utils.ASTUtils;
import npex.synthesizer.initializer.ValueInitializer;
import npex.synthesizer.template.PatchTemplateTernary;
import spoon.reflect.code.CtExpression;

abstract public class AbstractReplaceStrategy implements PatchStrategy<PatchTemplateTernary> {
  static Logger logger = LoggerFactory.getLogger(AbstractSkipStrategy.class);
  static private int idx = 0;

  final protected ValueInitializer initializer;

  public AbstractReplaceStrategy(ValueInitializer initializer) {
    this.initializer = initializer;
  }

  public String getName() {
    return this.getClass().getSimpleName() + initializer.getName();
  }

  public boolean isApplicable(CtExpression nullExp) {
    if (extractExprToReplace(nullExp).getType().toString().equals("void"))
      return false;

    if (extractExprToReplace(nullExp).equals(ASTUtils.getNearestSkippableStatement(nullExp))) {
      return false;
    }
    return true;
  }

  public List<PatchTemplateTernary> enumerate(CtExpression nullExp) {
    List<PatchTemplateTernary> templates = new ArrayList<>();
    int line = nullExp.getPosition().getLine();
    CtExpression expFrom = extractExprToReplace(nullExp);
    for (CtExpression expTo : enumerateAvailableExpressions(expFrom)) {
      String id = String.format("%s_%d_%d", this.getName(), line, idx);
      templates.add(new PatchTemplateTernary(id, nullExp, expFrom, expTo));
      idx++;
    }

    return templates;
  }

  protected abstract List<CtExpression> enumerateAvailableExpressions(CtExpression e);

  protected abstract CtExpression extractExprToReplace(CtExpression nullExp);
}