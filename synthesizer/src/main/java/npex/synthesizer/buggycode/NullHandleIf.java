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
package npex.synthesizer.buggycode;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import npex.synthesizer.Utils;
import spoon.javadoc.internal.Pair;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;

public class NullHandleIf extends NullHandle {
  Pair<CtBlock<?>, CtBlock<?>> branches;

  public NullHandleIf(CtIf handle) throws IllegalArgumentException {
    super(handle, handle.getCondition());
    this.branches = new Pair<>(handle.getThenStatement(), handle.getElseStatement());
  }

  public CtBlock<?> getNullBlock() {
    return isCondKindEqual() ? branches.a : branches.b;
  }

  public CtBlock<?> getNonNullBlock() {
    CtBlock<?> blk = !isCondKindEqual() ? branches.a : branches.b;
    if (blk != null)
      return blk;

    if (this.isFlowBreak()) {
      int idx = this.parentBlock.getStatements().indexOf(this.handle);
      CtBlock<?> nonNullBlock = this.parentBlock.getFactory().createBlock();
      this.parentBlock.getStatements().stream().skip(idx + 1).forEach(x -> {
        nonNullBlock.addStatement(x.clone());
      });

      return nonNullBlock;
    }

    return null;
  }

  private boolean isFlowBreak() {
    if (this.getNullBlock() != null)
      return this.getNullBlock().getStatements().stream().anyMatch(x -> x instanceof CtCFlowBreak);
    return false;
  }

  public void stripNullHandle(CtClass<?> klass) throws ArrayIndexOutOfBoundsException, NoSuchElementException {
    boolean isElseBlockEmpty = branches.b != null ? branches.b.getStatements().isEmpty() : true;
    int pos = parentBlock.getStatements().indexOf(handle);
    CtBlock<?> blk = Utils.findMatchedElement(klass, parentBlock);
    if (isFlowBreak() && isElseBlockEmpty) {
      // For null-return handles without an else-block, we add explicit block
      // for the non-null block so that it can be matched properly in the cloned AST.
      ListIterator<CtStatement> iter = blk.getStatements().listIterator(pos);
      List<CtStatement> stmtsToRemove = new ArrayList<>();
      while (iter.hasNext()) {
        stmtsToRemove.add(iter.next());
      }
      stmtsToRemove.forEach(s -> blk.removeStatement(s));
      blk.addStatement(getNonNullBlock());
    } else {
      blk.getStatement(pos).replace(getNonNullBlock());
    }
  }
}