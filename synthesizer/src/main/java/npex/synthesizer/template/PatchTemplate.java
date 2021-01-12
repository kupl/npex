package npex.synthesizer.template;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.synthesizer.Utils;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.AbstractFilter;

public abstract class PatchTemplate {
  static Logger logger = LoggerFactory.getLogger(PatchTemplate.class);

  protected final String id;
  protected final CtExecutable ast;
  protected final CtExpression nullExp;
  protected final Factory factory;
  private CtExecutable changed = null;

  public PatchTemplate(String id, CtExpression nullExp) {
    this.id = id;
    this.ast = nullExp.getParent(new AbstractFilter<CtExecutable>() {
      @Override
      public boolean matches(CtExecutable executable) {
        return executable instanceof CtMethod || executable instanceof CtConstructor;
      }
    }).clone();

    this.nullExp = Utils.findMatchedElementLookParent(nullExp, ast);
    this.factory = nullExp.getFactory();
  }

  public String getID() {
    return id;
  }

  public CtStatement getPatchedStatement() {
    return null;
  }

  /* Apply patch template and generate a fresh AST */
  public CtExecutable apply() {
    return (changed == null) ? (changed = implement()) : changed;
  }

  public void store(String projectRootPath, File outputDir) throws IOException {
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

  protected abstract CtExecutable implement();

}