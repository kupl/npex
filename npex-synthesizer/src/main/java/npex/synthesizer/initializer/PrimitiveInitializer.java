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
package npex.synthesizer.initializer;

import java.util.stream.Stream;

import npex.common.CommonExpressionTable;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

public class PrimitiveInitializer extends ValueInitializer<CtExpression> {
  public String getName() {
    return "Literal";
  }

  public CtExpression convertToCtExpression(CtExpression e) {
    return e;
  }

  protected Stream<CtExpression> enumerate(CtExpression expr) {
    CtTypeReference typ = expr.getType();
    if (typ != null && typ.isPrimitive()) {
      return CommonExpressionTable.find(typ).stream();
    }

    return Stream.empty();
  }
}