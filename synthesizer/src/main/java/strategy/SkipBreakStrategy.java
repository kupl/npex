package npex.synthesizer.strategy;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLoop;
import spoon.reflect.declaration.CtElement;

public class SkipBreakStrategy extends SkipLoopStrategy {
  public SkipBreakStrategy() {
    this.name = "SkipBreak";
  }

  @Override
  protected List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    ArrayList<CtElement> ret = new ArrayList<>();
    ret.add(nullExp.getFactory().createBreak());
    return ret;
  }

  @Override
  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return nullExp.getParent(CtLoop.class);
  }
}