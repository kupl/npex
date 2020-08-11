package npex.strategy;

import org.apache.log4j.Logger;

import npex.Utils;
import npex.template.PatchTemplate;
import npex.template.PatchTemplateSynth;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtElement;

public abstract class AbstractStrategy implements PatchStrategy {
  protected String name;

  protected Logger logger = Logger.getLogger(AbstractStrategy.class);

  final public String getName() {
    return this.name;
  }

  final protected String getPatchID(CtElement from, CtElement to, CtStatement nullStmt) {
    final int lineFrom = from.getPosition().getLine();
    final int lineTo = (to != null) ? to.getPosition().getLine() : lineFrom;
    // String nullStmtString = nullStmt != null ? name: "";
    // return String.format("%s_%d-%d_%s", this.getName(), lineFrom, lineTo,
    // nullStmtString);
    return String.format("%s_%d-%d", this.getName(), lineFrom, lineTo);
  }

  protected CtElement createSkipFrom(CtExpression<?> nullExp) {
    return Utils.getEnclosingStatement(nullExp);
  }

  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return Utils.getEnclosingStatement(nullExp);
  }

  final public PatchTemplate generate(CtExpression<?> nullExp) {
    final CtElement skipFrom = this.createSkipFrom(nullExp);
    final CtElement skipTo = this.createSkipTo(nullExp);
    final CtStatement nullBlockStmt = createNullBlockStmt(nullExp);
    final String patchID = this.getPatchID(skipFrom, skipTo, nullBlockStmt);
    return new PatchTemplateSynth(patchID, nullExp, nullBlockStmt, skipFrom, skipTo);
  }

  abstract CtStatement createNullBlockStmt(CtExpression<?> nullExp);
}