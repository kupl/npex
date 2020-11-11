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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

public final class DefaultValueTable {
  static Map<String, List<String>> table = new HashMap<>();
  static {
    table.put("int", Arrays.asList(new String[] { "0", "1" }));
    table.put("String", Arrays.asList(new String[] { "null", "\"\"" }));
    table.put("boolean", Arrays.asList(new String[] { "false", "true" }));
    table.put("double", Arrays.asList(new String[] { "0.0", "1.0" }));
    table.put("float", Arrays.asList(new String[] { "0.0", "1.0" }));
    table.put("void", Arrays.asList(new String[] { "" }));
  }

  static boolean hasDefaultValue(CtTypeReference<?> typ) {
    return table.containsKey(typ.getSimpleName());
  }

  static <T> List<CtExpression<T>> getDefaultValues(CtTypeReference<T> typ) {
    return table.getOrDefault(typ.getSimpleName(), Collections.singletonList("null")).stream().map(s -> {
      CtCodeSnippetExpression<T> exp = typ.getFactory().createCodeSnippetExpression();
      exp.setValue(s);
      exp.setType(typ);
      return exp;
    }).collect(Collectors.toList());
  }
}
