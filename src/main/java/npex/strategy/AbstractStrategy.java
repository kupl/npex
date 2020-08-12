package npex.strategy;

import org.apache.log4j.Logger;

import npex.Utils;
import npex.template.PatchTemplate;
import npex.template.PatchTemplateSynth;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public abstract class AbstractStrategy implements PatchStrategy {
  protected String name;

  protected Logger logger = Logger.getLogger(AbstractStrategy.class);

  final public String getName() {
    return this.name;
  }

  final protected String getPatchID(CtElement from, CtElement to) {
    final int lineFrom = from.getPosition().getLine();
    final int lineTo = (to != null) ? to.getPosition().getLine() : lineFrom;
    return String.format("%s_%d-%d", this.getName(), lineFrom, lineTo);
  }

  protected CtElement createSkipFrom(CtExpression<?> nullExp) {
    return Utils.getEnclosingStatement(nullExp);
  }

  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return Utils.getEnclosingStatement(nullExp);
  }

  public PatchTemplate generate(CtExpression<?> nullExp) {
    final CtElement skipFrom = this.createSkipFrom(nullExp);
    final CtElement skipTo = this.createSkipTo(nullExp);
    final CtElement nullBlockStmt = createNullBlockStmt(nullExp);
    final String patchID = this.getPatchID(skipFrom, skipTo);
    return new PatchTemplateSynth(patchID, nullExp, nullBlockStmt, skipFrom, skipTo);
  }

  abstract CtElement createNullBlockStmt(CtExpression<?> nullExp);
}