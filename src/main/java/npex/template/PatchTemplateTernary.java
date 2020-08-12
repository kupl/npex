package npex.template;

import org.apache.log4j.Logger;

import npex.Utils;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;

/* TODO: templates 모듈의 구조가 이상함. PatchTemplateSynth를 추상화하고 If, Ternary 모양으로 분리해야 함.
우선은 동작하도록 구현하고 리팩토링 할 것. */
public class PatchTemplateTernary extends PatchTemplateSynth {
  CtExpression<?> nullBlockStmt;
  CtExpression<?> skipExpr;
  CtStatement patchedStatement;

  static Logger logger = Logger.getLogger(PatchTemplateTernary.class);

  public PatchTemplateTernary(String ID, CtExpression<?> nullExp, CtExpression<?> nullBlockStmt,
      CtExpression<?> skipFrom, CtExpression<?> skipTo) {
    super(ID, nullExp, nullBlockStmt, skipFrom, skipTo);
    this.nullBlockStmt = nullBlockStmt;
    this.patchedStatement = skipFrom.getParent(CtStatement.class);
    skipExpr = skipFrom;
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

  @Override
  public CtMethod<?> implement() {
    CtExpression<?> target = Utils.findMatchedElement(targetClass, skipExpr);
    CtConditional<?> ternary = createTernary(nullBlockStmt, target);
    target.replace(ternary);
    patchedStatement = target.getParent(CtStatement.class);
    return target.getParent(CtMethod.class);
  }

  @Override
  public CtBlock<?> getBlock() {
    return patchedStatement.getParent(CtBlock.class);
  }

  @Override
  public SourceChange<CtMethod<?>> getSourceChange() {
    CtMethod<?> originalMethod = nullExp.getParent(CtMethod.class);
    return new SourceChange<>(originalMethod, this.apply(), this.patchedStatement);
  }
}