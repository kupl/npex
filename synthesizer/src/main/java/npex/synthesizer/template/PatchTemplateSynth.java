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
package npex.synthesizer.template;

import npex.synthesizer.template.SkipRange.Kind;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

public class PatchTemplateSynth implements PatchTemplate {
  final String ID;
  final CtExpression<?> nullExp;
  final CtElement nullBlockStmt;

  final SkipRange range;
  final Factory factory;
  final CtIf ifStmt;
  final CtClass<?> targetClass;
  CtMethod<?> patchedMethod = null;

  public PatchTemplateSynth(String ID, CtExpression<?> nullExp, CtElement nullBlockStmt, CtElement skipFrom,
      CtElement skipTo) {
    this.ID = String.format("%s", ID);
    this.nullExp = nullExp;
    this.nullBlockStmt = nullBlockStmt;

    this.targetClass = nullExp.getParent(CtClass.class).clone();
    this.range = new SkipRange(targetClass, skipFrom, skipTo);
    this.factory = nullExp.getFactory();
    this.ifStmt = factory.createIf();
  }

  public String getID() {
    return this.ID;
  }

  public CtBlock<?> getBlock() {
    return this.ifStmt.getParent(CtBlock.class);
  }

  public CtMethod<?> apply() {
    return patchedMethod != null ? patchedMethod : (patchedMethod = implement());
  }

  public SourceChange<CtMethod<?>> getSourceChange() {
    CtMethod<?> originalMethod = nullExp.getParent(CtMethod.class);
    return new SourceChange<>(originalMethod, this.apply(), this.ifStmt);
  }

  CtBinaryOperator<Boolean> createNullCond(boolean isNullCheck) {
    CtBinaryOperator<Boolean> nullCond = factory.createBinaryOperator();
    CtCodeSnippetExpression<?> nullLit = factory.createCodeSnippetExpression();
    nullLit.setValue("null");
    nullCond.setKind(isNullCheck ? BinaryOperatorKind.EQ : BinaryOperatorKind.NE);
    nullCond.setLeftHandOperand((CtExpression<?>) nullExp.clone());
    nullCond.setRightHandOperand(nullLit.compile());
    return nullCond;
  }

  CtMethod<?> implement() {
    boolean isNullCheck = range.kind != Kind.Normal && range.kind != Kind.Loop;
    ifStmt.setCondition(createNullCond(isNullCheck));
    if (range.kind == Kind.Normal || range.kind == Kind.Loop) {
      range.replaceSkipRange(ifStmt);
      if (nullBlockStmt != null)
        ifStmt.setElseStatement((CtStatement) nullBlockStmt);
    } else {
      ifStmt.setThenStatement((CtStatement) nullBlockStmt);
      range.getSkipFromStmt().insertBefore(ifStmt);
    }

    return ifStmt.getParent(CtMethod.class);
  }
}