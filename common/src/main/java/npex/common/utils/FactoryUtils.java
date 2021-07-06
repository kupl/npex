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
package npex.common.utils;

import java.util.Collection;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.factory.CoreFactory;
import spoon.reflect.factory.TypeFactory;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.DefaultCoreFactory;

public class FactoryUtils {
  static CoreFactory factory = new DefaultCoreFactory();
  static TypeFactory tFactory = new TypeFactory();

  public static CtTypeReference<java.lang.Void> VOID_TYPE = tFactory.VOID_PRIMITIVE;
  public static CtTypeReference<java.lang.Object> OBJECT_TYPE = tFactory.OBJECT;
  public static CtTypeReference<java.lang.String> STRING_TYPE = tFactory.STRING;
  public static CtTypeReference<java.lang.Class> CLASS_TYPE = tFactory.createReference(java.lang.Class.class);
  public static CtTypeReference<java.util.Collection> COLLECTION_TYPE = tFactory
      .createReference(java.util.Collection.class);

  public static CtLiteral<Boolean> createBooleanLiteral(boolean boolValue) {
    CtLiteral<Boolean> lit = factory.createLiteral();
    lit.setType(tFactory.BOOLEAN_PRIMITIVE);
    lit.setValue(boolValue);
    return lit;
  }

  public static CtLiteral createNullLiteral() {
    return factory.createLiteral().setValue(null);
  }

  public static CtLiteral createEmptyString() {
    return factory.createLiteral().setValue("");
  }

}