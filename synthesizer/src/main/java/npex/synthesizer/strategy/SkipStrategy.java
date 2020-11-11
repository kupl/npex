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

import java.util.Collections;
import java.util.List;

import npex.synthesizer.Utils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.SameFilter;

public abstract class SkipStrategy extends AbstractStrategy {
  protected List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    return Collections.singletonList(null);
  }

  public final boolean isApplicable(CtExpression<?> nullExp) {
    /* A skip strategy is basically inapplicable to null source */
    if (nullExp.toString().equals("null")) {
      return false;
    }

    /* Lambda is tricky */
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

  /* Implement this method for an additional strategy-specific check */
  abstract protected boolean _isApplicable(CtExpression<?> nullExp);
}