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

import npex.synthesizer.Utils;
import npex.synthesizer.template.PatchTemplateIf;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.CoreFactory;
import spoon.reflect.visitor.filter.SameFilter;
import spoon.support.DefaultCoreFactory;

public abstract class AbstractSkipStrategy implements PatchStrategy<PatchTemplateIf> {
  static Logger logger = LoggerFactory.getLogger(AbstractSkipStrategy.class);
  static private int idx = 0;
  static CoreFactory factory = new DefaultCoreFactory();

  protected CtStatement createSkipFrom(CtExpression nullExp) {
    return Utils.getNearestSkippableStatement(nullExp);
  }

  protected CtStatement createSkipTo(CtExpression nullExp) {
    return Utils.getNearestSkippableStatement(nullExp);
  }

  public boolean isApplicable(CtExpression<?> nullExp) {
    /* A skip strategy is basically inapplicable to null-sources */
    if (nullExp.toString().equals("null")) {
      return false;
    }

    /* Lambda is tricky so we currently skip those cases */
    if (nullExp.getParent(CtLambda.class) != null) {
      return false;
    }

    /* Do not skip if null expr is a loop-head element */
    CtLoop loop = nullExp.getParent(CtLoop.class);
    if (loop != null) {
      CtElement loopHead = Utils.getLoopHeadElement(loop);
      if (loopHead != null && !loopHead.getElements(new SameFilter(nullExp)).isEmpty())
        return false;
    }
    return _isApplicable(nullExp);
  }

  public List<PatchTemplateIf> enumerate(CtExpression<?> nullExp) {
    final CtStatement skipFrom = this.createSkipFrom(nullExp);
    final CtStatement skipTo = this.createSkipTo(nullExp);
    List<PatchTemplateIf> templates = new ArrayList<>();
    for (CtStatement stmt : createNullExecStatements(nullExp)) {
      final int lineFrom = skipFrom.getPosition().getLine();
      final int lineTo = (skipTo != null) ? skipTo.getPosition().getLine() : lineFrom;
      String id = String.format("%s_%d-%d_%d", this.getName(), lineFrom, lineTo, idx);
      templates.add(new PatchTemplateIf(id, nullExp, stmt, skipFrom, skipTo));
      idx += 1;
    }

    return templates;
  }

  /*
   * Implement this if a strategy generates patch including null-execution
   * statements such as control-flow breaks or pointer initialization.
   */
  protected abstract List<CtStatement> createNullExecStatements(CtExpression nullExp);

  /* Implement this for an additional strategy-specific check */
  protected abstract boolean _isApplicable(CtExpression<?> nullExp);
}