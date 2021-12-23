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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.filters.MethodOrConstructorFilter;
import npex.common.utils.ASTUtils;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.factory.CoreFactory;
import spoon.support.DefaultCoreFactory;

public abstract class PatchTemplate {
  static Logger logger = LoggerFactory.getLogger(PatchTemplate.class);

  protected final String id;
  protected final CtExecutable ast;
  protected final CtExecutable astOrg;
  protected final CtExpression nullExp;
  private CtExecutable changed = null;

  private final CtExpression nullExpOrg;

  protected final CoreFactory factory = new DefaultCoreFactory();

  public PatchTemplate(String id, CtExpression nullExpOrg) {
    this.id = id;
    this.astOrg = nullExpOrg.getParent(new MethodOrConstructorFilter());
    this.ast = astOrg.clone();
    this.ast.setParent(astOrg.getParent());
    this.nullExpOrg = nullExpOrg;
    this.nullExp = ASTUtils.findMatchedElementLookParent(ast, nullExpOrg);
  }

  public String getID() {
    return id;
  }

  public CtStatement getOriginalStatement() {
    return ASTUtils.getEnclosingStatement(nullExpOrg);
  }

  /* Apply patch template and generate a fresh AST */
  public CtExecutable apply() {
    return (changed == null) ? (changed = implement()) : changed;
  }

  public void store(String projectRootPath, File outputDir) throws IOException {
    assert (changed != null);
    SourceChange change = new SourceChange(astOrg, ast, getPatchedStatement());
    change.store(projectRootPath, outputDir);
  }

  protected CtBinaryOperator<Boolean> createNullCond(boolean isEqualToNull) {
    CtBinaryOperator<Boolean> nullCond = factory.createBinaryOperator();
    CtCodeSnippetExpression<?> nullLit = factory.createCodeSnippetExpression();
    nullLit.setValue("null");
    nullCond.setKind(isEqualToNull ? BinaryOperatorKind.EQ : BinaryOperatorKind.NE);
    nullCond.setLeftHandOperand(nullExp.clone());
    nullCond.setRightHandOperand(nullLit.compile());
    return nullCond;
  }

  public abstract CtStatement getPatchedStatement();

  protected abstract CtExecutable implement() throws ImplementationFailure;

}