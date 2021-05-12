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

import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.filter.TypeFilter;

@SuppressWarnings("rawtypes")
public class VarInitializer extends ValueInitializer<CtVariable> {
  public String getName() {
    return "Var";
  }

  protected Stream<CtVariable> enumerate(CtExpression expr) {
    CtExecutable executable = expr.getParent(CtMethod.class) != null ? expr.getParent(CtMethod.class)
        : expr.getParent(CtConstructor.class);
    Stream<CtVariable> localVars = executable.getElements(new TypeFilter<>(CtVariable.class)).stream();
    Stream<CtVariable> classMembers = expr.getParent(CtClass.class).getAllFields().stream()
        .map(f -> f.getDeclaration());

    return Stream.concat(localVars, classMembers).filter(v -> v != null && !v.getSimpleName().equals(expr.toString()));
  }

  protected CtExpression convertToCtExpression(CtVariable var) {
    return var.getFactory().createVariableRead(var.getReference(), var.isStatic());
  }
}