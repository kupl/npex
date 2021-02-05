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
package npex.extractor.nullhandle;

import java.util.List;

import npex.common.filters.MethodOrConstructorFilter;
import npex.common.utils.ASTUtils;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableWrite;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.reference.CtVariableReference;
import spoon.reflect.visitor.EarlyTerminatingScanner;

public class SkipNullHandle extends AbstractNullHandle<CtIf> {
  public SkipNullHandle(CtIf handle, CtBinaryOperator nullCond) {
    super(handle, nullCond);
  }

  @Override
  protected AbstractNullModelScanner createNullModelScanner(CtExpression nullExp) {
    return new NullModelScanner(nullExp);
  }

  private class NullModelScanner extends AbstractNullModelScanner {
    private CtStatement firstStmt;

    public NullModelScanner(CtExpression nullExp) {
      super(nullExp);
    }

    @Override
    public void visitCtBlock(CtBlock blk) {
      List<CtStatement> stmts = blk.getStatements();
      if (!handle.getThenStatement().equals(blk) || stmts.size() != 1) {
        return;
      }
      firstStmt = stmts.get(0);
      if ((firstStmt instanceof CtInvocation || firstStmt instanceof CtAssignment)) {
        super.visitCtBlock(blk);
      } else {
        terminate();
      }
    }

    @Override
    protected NullModel createModel(CtInvocation invo) {
      if (firstStmt instanceof CtAssignment a) {
        if (a.getAssigned() instanceof CtVariableWrite w && w.getVariable() instanceof CtLocalVariableReference) {
          var scanner = new NullValueScanner(a);
          invo.getParent(new MethodOrConstructorFilter()).accept(scanner);
          return new NullModel(nullExp, firstStmt, scanner.getResult());
        }
      }

      return new NullModel(nullExp, firstStmt, null);
    }

    private class NullValueScanner extends EarlyTerminatingScanner<CtExpression> {
      private final CtAssignment me;
      private final CtVariableReference varRef;

      private CtAssignment lastAssignment;

      public NullValueScanner(CtAssignment me) {
        this.me = me;
        this.varRef = ((CtVariableWrite) me.getAssigned()).getVariable();
      }

      @Override
      public void visitCtAssignment(CtAssignment assignment) {
        super.visitCtAssignment(assignment);
        // If the given assignment is the first one, null-value is a default value.
        if (assignment == me && lastAssignment == null) {
          setResult(getDefaultValue(varRef.getType()));
          terminate();
          return;
        }

        if (assignment.getAssigned() instanceof CtVariableWrite write && write.getVariable().equals(varRef)) {
          if (assignment == me) {
            CtBlock lastAssignmentBlock = lastAssignment.getParent(CtBlock.class);
            CtBlock myBlock = me.getParent(CtBlock.class);

            if (ASTUtils.isChildOf(myBlock, lastAssignmentBlock)) {
              setResult(lastAssignment.getAssignment());
            }
            terminate();
          } else {
            lastAssignment = assignment;
          }
        }
      }

      private CtExpression getDefaultValue(CtTypeReference type) {
        String value;
        switch (type.getSimpleName()) {
          case "short", "int", "long":
            value = "0";
            break;
          case "boolean":
            value = "false";
            break;
          default:
            value = null;
        }
        return factory.createCodeSnippetExpression().setValue(value);
      }
    }
  }
}