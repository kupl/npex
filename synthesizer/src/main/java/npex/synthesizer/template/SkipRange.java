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

import java.util.List;

import npex.synthesizer.Utils;
import spoon.SpoonException;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.path.CtRole;

public class SkipRange {
  public final CtElement from;
  public final CtElement to;
  public final Kind kind;

  enum Kind {
    Loop, Return, Break, Continue, Nothing, Normal
  }

  private static Kind getKind(CtElement from, CtElement to) {
    if (from instanceof CtLoop && to instanceof CtLoop)
      return Kind.Loop;
    if (to instanceof CtMethod<?>)
      return Kind.Return;
    else if (to instanceof CtLoop)
      return Kind.Break;
    else if (to == null)
      return Kind.Nothing;

    CtLoop enclosingLoop = to.getParent(CtLoop.class);
    if (enclosingLoop != null) {
      CtBlock<?> loopBody = (CtBlock<?>) enclosingLoop.getBody();
      if (to.equals(loopBody.getLastStatement())) {
        return Kind.Continue;
      }
    }
    return Kind.Normal;
  }

  public SkipRange(CtClass<?> targetClass, CtElement from, CtElement to) {
    this.from = Utils.findMatchedElement(targetClass, from);
    this.to = (to != null) ? Utils.findMatchedElement(targetClass, to) : null;
    this.kind = getKind(from, to);
  }

  public CtStatement getSkipFromStmt() {
    return (from instanceof CtStatement) ? (CtStatement) from : from.getParent(CtStatement.class);
  }

  public void replaceSkipRange(CtIf ifStmt) throws IllegalArgumentException, SpoonException {
    if (!(kind == Kind.Normal || kind == Kind.Loop))
      throw new IllegalArgumentException();

    if (from.equals(to) && from instanceof CtBlock) {
      replaceSkipRange(ifStmt, (CtBlock<?>) from);
      return;
    }

    CtBlock<?> skipBlock = this.from.getParent(CtBlock.class);
    List<CtStatement> stmts = skipBlock.getStatements();
    int idxFrom = stmts.indexOf(this.from);
    int idxTo = stmts.indexOf(this.to);

    /* skip range should be within the same block */
    assert (skipBlock.equals(this.to.getParent(CtBlock.class)));
    assert (idxFrom > -1 && idxTo > -1);
    CtBlock<?> thenBlock = ifStmt.getFactory().createBlock();
    ifStmt.setThenStatement(thenBlock);
    for (CtStatement stmt : stmts.subList(idxFrom, idxTo + 1)) {
      skipBlock.removeStatement(stmt);
      thenBlock.addStatement(stmt);
    }
    skipBlock.addStatement(idxFrom, ifStmt);
  }

  private void replaceSkipRange(CtIf ifStmt, CtBlock<?> block) {
    CtBlock<?> thenBlock = ifStmt.getFactory().createBlock();
    ifStmt.setThenStatement(thenBlock);
    thenBlock.addStatement(block.clone());

    // For the case where explicit block parentheses are needed, e.g., a lambda body
    if (block.getRoleInParent().equals(CtRole.BODY)) {
      CtBlock<?> wrappingBlock = ifStmt.getFactory().createBlock();
      wrappingBlock.addStatement(ifStmt);
      block.replace(wrappingBlock);
      return;
    }

    block.replace(ifStmt);
  }
}