package npex.buggycode;

import java.util.ArrayList;
import java.util.List;

import npex.Utils;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;

public class NullHandleTernary extends NullHandle {
  CtConditional<?> conditional;

  public static List<NullHandle> collect(CtModel model) {
    return new ArrayList<>();
  }

  public NullHandleTernary(CtConditional<?> conditional) {
    super(conditional.getParent(CtStatement.class), conditional.getCondition());
    this.conditional = conditional;
  }

  public void stripNullHandle(CtClass<?> klass) {
    CtConditional<?> ternaryClone = Utils.findMatchedElement(klass, conditional);
    CtExpression<?> expr = isCondKindEqual() ? conditional.getElseExpression() : conditional.getThenExpression();
    ternaryClone.replace(expr);
  }
}