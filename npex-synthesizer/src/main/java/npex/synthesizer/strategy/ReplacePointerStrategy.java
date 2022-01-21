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

import java.util.List;
import java.util.stream.Stream;

import npex.synthesizer.enumerator.ExpressionEnumerator;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtRole;
import spoon.reflect.reference.CtTypeReference;

public class ReplacePointerStrategy extends AbstractReplaceStrategy {

  @Override
  protected List<CtExpression> extractExprToReplace(CtExpression nullExp) {
    return List.of(nullExp);
  }

  private boolean isLiteralNull(CtExpression nullExp) {
    return nullExp.getType().toString().equals(CtTypeReference.NULL_TYPE_NAME);
  }

  private CtTypeReference getNullValueType(CtExpression nullExp) {
    CtElement parent = nullExp.getParent();
    if (parent instanceof CtAssignment assign) {
      return assign.getAssigned().getType();
    } else if (parent instanceof CtLocalVariable var) {
      return var.getReference().getType();
    } else if (nullExp.getRoleInParent().equals(CtRole.ARGUMENT)) {
      CtInvocation invo = nullExp.getParent(CtInvocation.class);
      Stream<CtExpression<?>> args = invo.getArguments().stream();
      return (CtTypeReference) args.map(arg -> arg.getType()).toArray()[invo.getArguments().indexOf(nullExp)];
    } else {
      throw new IllegalArgumentException("Not supported null assignment form");
    }
  }

  protected List<CtExpression> enumerateAvailableExpressions(CtExpression nullExp) {
    CtTypeReference typ = !isLiteralNull(nullExp) ? nullExp.getType() : getNullValueType(nullExp);
    List<CtExpression> typeCompatibleExprs = ExpressionEnumerator.enumTypeCompatibleExpressions(nullExp, typ);
    return typeCompatibleExprs;
  }
}