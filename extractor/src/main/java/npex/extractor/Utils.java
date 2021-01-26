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
package npex.extractor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.CoreFactory;
import spoon.reflect.visitor.Filter;
import spoon.support.DefaultCoreFactory;

public class Utils {
  static Logger logger = LoggerFactory.getLogger(Utils.class);
  static CoreFactory factory = new DefaultCoreFactory();

  public static boolean isNullCondition(CtBinaryOperator bo) {
    return bo.getRightHandOperand() instanceof CtLiteral lit && lit.toString().equals("null");
  }

  public static boolean isChildOf(CtElement el, CtElement root) {
    return !root.filterChildren(new EqualsFilter(el)).list().isEmpty();
  }

  public static CtLiteral createNullLiteral() {
    return factory.createLiteral().setValue(null);

  }

  static class EqualsFilter implements Filter<CtElement> {
    CtElement query;

    public EqualsFilter(CtElement query) {
      this.query = query;
    }

    @Override
    public boolean matches(CtElement e) {
      return e.equals(query);
    }

  }
}