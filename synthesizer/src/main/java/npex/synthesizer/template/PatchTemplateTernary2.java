package npex.synthesizer.template;

import npex.synthesizer.Utils;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtExecutable;

public class PatchTemplateTernary2 extends PatchTemplate2 {
  private final CtExpression exprToReplace;
  private final CtExpression alternativeValue;

  public PatchTemplateTernary2(String id, CtExpression nullExp, CtExpression exprToReplace,
      CtExpression alternativeValue) {
    super(id, nullExp);
    this.exprToReplace = Utils.findMatchedElementLookParent(exprToReplace, ast);
    this.alternativeValue = alternativeValue;
  }

  protected CtExecutable implement() {
    CtConditional ternary = factory.createConditional();
    ternary.setCondition(createNullCond(false));
    ternary.setThenExpression(exprToReplace.clone());
    ternary.setElseExpression(alternativeValue);

    exprToReplace.replace(ternary);
    return ast;
  }
}