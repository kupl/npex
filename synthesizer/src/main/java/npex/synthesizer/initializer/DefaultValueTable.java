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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

public final class DefaultValueTable {
  static Map<String, List<String>> table = new HashMap<>();
  static {
    table.put("int", Arrays.asList(new String[] { "0", "1" }));
    table.put("java.lang.String", Arrays.asList(new String[] { "null", "\"\"" }));
    table.put("boolean", Arrays.asList(new String[] { "false", "true" }));
    table.put("double", Arrays.asList(new String[] { "0.0", "1.0" }));
    table.put("float", Arrays.asList(new String[] { "0.0", "1.0" }));
  }

  static boolean hasDefaultValue(CtTypeReference<?> typ) {
    return table.containsKey(typ.getSimpleName());
  }

  public static <T> List<CtLiteral<T>> getDefaultValues(CtTypeReference<T> typ) throws IllegalArgumentException {
    List<CtLiteral<T>> values = new ArrayList<>();
    Factory factory = typ.getFactory();
    if (typ.getSimpleName().equals("void")) {
      return values;
    }
    for (String s : table.getOrDefault(typ.getQualifiedName(), Collections.singletonList("null"))) {
      CtCodeSnippetExpression e = factory.createCodeSnippetExpression(s);
      CtLiteral lit = factory.createLiteral(e.compile());
      values.add((CtLiteral) lit.setType(typ));
    }

    return values;
  }
}
