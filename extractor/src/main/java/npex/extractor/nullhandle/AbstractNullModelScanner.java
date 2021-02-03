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

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.factory.CoreFactory;
import spoon.reflect.visitor.EarlyTerminatingScanner;
import spoon.support.DefaultCoreFactory;

public abstract class AbstractNullModelScanner extends EarlyTerminatingScanner<List<NullModel>> {
  final protected List<NullModel> models = new ArrayList<NullModel>();
  final protected CoreFactory factory = new DefaultCoreFactory();
  final protected CtExpression nullExp;

  public AbstractNullModelScanner(CtExpression nullExp) {
    this.nullExp = nullExp;
    setResult(models);
  }

  @Override
  public void visitCtInvocation(CtInvocation invo) {
    super.visitCtInvocation(invo);
    if (invo.getTarget().equals(nullExp) || invo.getArguments().contains(nullExp)) {
      models.add(createModel(invo));
      terminate();
    }
  }

  protected abstract NullModel createModel(CtInvocation invo);

}