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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtExecutable;

public class PatchTemplateIf extends PatchTemplate {
  private enum SkipKind {
    SKIPONLY, DOSMTH
  };

  private final CtStatement nullExecStmt;
  private final CtStatement skipFrom, skipTo;
  private final SkipKind kind;

  public PatchTemplateIf(String id, CtExpression nullExp, CtStatement nullExecStmt, CtStatement skipFrom,
      CtStatement skipTo) {
    super(id, nullExp);
    this.nullExecStmt = nullExecStmt;
    this.skipFrom = ASTUtils.findMatchedElementLookParent(ast, skipFrom);
    this.skipTo = skipTo != null ? ASTUtils.findMatchedElementLookParent(ast, skipTo) : null;

    this.kind = (nullExecStmt == null) ? SkipKind.SKIPONLY : SkipKind.DOSMTH;
  }

  protected CtExecutable implement() throws ImplementationFailure {
    CtIf ifStmt = factory.createIf();
    CtBlock<?> thenBlock = factory.createBlock();
    ifStmt.setThenStatement(thenBlock);
    try {
      CtBlock<?> modBlock = skipFrom.getParent(CtBlock.class);
      List<CtStatement> stmts = new ArrayList<>(modBlock.getStatements());
      int idxFrom = stmts.indexOf(skipFrom);
      int idxTo = stmts.indexOf(skipTo);
      switch (kind) {
        case SKIPONLY:
          ifStmt.setCondition(createNullCond(false));
          for (CtStatement s : stmts.subList(idxFrom, idxTo + 1)) {
            modBlock.removeStatement(s);
            thenBlock.addStatement(s);
          }
          modBlock.addStatement(idxFrom, ifStmt);
          break;
        case DOSMTH:
          ifStmt.setCondition(createNullCond(true));
          thenBlock.addStatement(nullExecStmt);
          modBlock.addStatement(idxFrom, ifStmt);
          break;
      }

      return ast;
    } catch (NullPointerException | IndexOutOfBoundsException | ConcurrentModificationException e) {
      throw new ImplementationFailure(this, e);
    }
  }
}