package npex.synthesizer.template;

import org.apache.log4j.Logger;

import npex.synthesizer.Utils;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;

/* TODO: templates 모듈의 구조가 이상함. PatchTemplateSynth를 추상화하고 If, Ternary 모양으로 분리해야 함.
우선은 동작하도록 구현하고 리팩토링 할 것. */
public class PatchTemplateTernary implements PatchTemplate {
  final String ID;
  final CtExpression<?> nullExp;
  CtExpression<?> nullBlockStmt;
  CtExpression<?> skipExpr;
  CtStatement patchedStatement;
  CtMethod<?> patchedMethod = null;

  final Factory factory;
  final CtClass<?> targetClass;
  final CtMethod<?> targetMethod;
  static Logger logger = Logger.getLogger(PatchTemplateTernary.class);

  public PatchTemplateTernary(String ID, CtExpression<?> nullExp, CtExpression<?> nullBlockStmt,
      CtExpression<?> skipFrom, CtExpression<?> skipTo) {
    this.ID = String.format("%s", ID);

    this.nullExp = nullExp;
    this.nullBlockStmt = nullBlockStmt;
    this.patchedStatement = skipFrom.getParent(CtStatement.class);
    this.targetClass = nullExp.getParent(CtClass.class).clone();
    this.targetMethod = nullExp.getParent(CtMethod.class).clone();
    this.factory = nullExp.getFactory();
    skipExpr = skipFrom;
  }

  public String getID() {
    return this.ID;
  }

  public CtMethod<?> apply() {
    return patchedMethod != null ? patchedMethod : (patchedMethod = implement());
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

  public <T> CtConditional<T> createTernary(CtExpression<T> value, CtExpression<?> replExpr) {
    CtBinaryOperator<Boolean> cond = createNullCond(false);
    CtExpression<T> target = Utils.findMatchedElement(targetClass, replExpr);
    CtConditional<T> conditional = factory.createConditional();
    conditional.setCondition(cond);
    conditional.setThenExpression(target.clone());
    conditional.setElseExpression((CtExpression<T>) value);
    return conditional;
  }

  public CtMethod<?> implement() {
    CtExpression<?> target = Utils.findMatchedElement(targetMethod, skipExpr);
    if (nullExp.toString().equals("null")) {
      target = Utils.findMatchedElementLookParent(targetMethod, skipExpr);
      target.replace(nullBlockStmt);
    } else {
      target.replace(createTernary(nullBlockStmt, target));
    }
    patchedStatement = target.getParent(CtStatement.class);
    return target.getParent(CtMethod.class);
  }

  public CtBlock<?> getBlock() {
    return patchedStatement.getParent(CtBlock.class);
  }

  public SourceChange<CtMethod<?>> getSourceChange() {
    CtMethod<?> originalMethod = nullExp.getParent(CtMethod.class);
    return new SourceChange<>(originalMethod, this.apply(), this.patchedStatement);
  }
}