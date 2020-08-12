package npex.template;

import npex.template.SkipRange.Kind;
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
    this.ID = ID;
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
    ifStmt.setCondition(createNullCond(range.kind != Kind.Normal));
    if (range.kind == Kind.Normal) {
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