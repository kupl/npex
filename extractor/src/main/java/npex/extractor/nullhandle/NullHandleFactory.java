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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.utils.ASTUtils;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtThrow;

public class NullHandleFactory {
  static Logger logger = LoggerFactory.getLogger(NullHandleFactory.class);

  public static AbstractNullHandle createNullHandle(CtCodeElement element) {
    AbstractNullHandle handle;
    if (element instanceof CtBinaryOperator bo) {
      handle = createBooleanNullHandle(bo);
    } else if (element instanceof CtConditional ternary) {
      handle = createTernaryNullHandle(ternary);
    } else if (element instanceof CtIf ifStmt) {
      handle = createSkipNullHandle(ifStmt);
      if (handle == null) {
        handle = createThrowNullHandle(ifStmt);
      }
    } else {
      return null;
    }

    if (handle == null) {
      logger.info("Could extract handle for {}", element);
    }

    return handle;
  }

  private static BooleanNullHandle createBooleanNullHandle(CtBinaryOperator bo) {
    if (bo.getLeftHandOperand()instanceof CtBinaryOperator cond && ASTUtils.isNullCondition(cond)) {
      return new BooleanNullHandle(bo, cond);
    }
    return null;
  }

  private static TernaryNullHandle createTernaryNullHandle(CtConditional ternary) {
    if (ternary.getCondition()instanceof CtBinaryOperator cond && ASTUtils.isNullCondition(cond)) {
      return new TernaryNullHandle(ternary, cond);
    }
    return null;
  }

  private static SkipNullHandle createSkipNullHandle(CtIf ifStmt) {
    if (ifStmt.getCondition()instanceof CtBinaryOperator cond && ASTUtils.isNullCondition(cond)) {
      if (cond.getKind().equals(BinaryOperatorKind.NE))
        return new SkipNullHandle(ifStmt, cond);
    }
    return null;
  }

  private static ThrowNullHandle createThrowNullHandle(CtIf ifStmt) {
    if (ifStmt.getCondition()instanceof CtBinaryOperator cond && ASTUtils.isNullCondition(cond)) {
      CtBlock blk = ifStmt.getThenStatement();
      if (blk == null || blk.getStatements().isEmpty()) {
        return null;
      }
      if (cond.getKind().equals(BinaryOperatorKind.EQ) && blk.getLastStatement()instanceof CtThrow thrownStmt) {
        return new ThrowNullHandle(ifStmt, cond, thrownStmt);
      }
    }

    return null;
  }

}