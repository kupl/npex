package npex.synthesizer.strategy;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtElement;

public class SkipContinueStrategy extends SkipLoopStrategy {
  public SkipContinueStrategy() {
    this.name = "SkipContinue";
  }

  @Override
  protected List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    ArrayList<CtElement> r = new ArrayList<>();
    r.add(nullExp.getFactory().createContinue());
    return r;
  }

  @Override
  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    CtBlock<?> loopBody = (CtBlock<?>) nullExp.getParent(CtLoop.class).getBody();
    return loopBody.getLastStatement();
  }
}