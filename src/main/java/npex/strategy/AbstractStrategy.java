package npex.strategy;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import npex.Utils;
import npex.template.PatchTemplate;
import npex.template.PatchTemplateSynth;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public abstract class AbstractStrategy implements PatchStrategy {
  protected String name;

  protected Logger logger = Logger.getLogger(AbstractStrategy.class);

  protected int idx = 0;

  final public String getName() {
    return this.name;
  }

  final protected String getPatchID(CtElement from, CtElement to) {
    final int lineFrom = from.getPosition().getLine();
    final int lineTo = (to != null) ? to.getPosition().getLine() : lineFrom;
    idx += 1;
    return String.format("%s_%d-%d_%d", this.getName(), lineFrom, lineTo, idx);
  }

  protected CtElement createSkipFrom(CtExpression<?> nullExp) {
    return Utils.getNearestSkippableStatement(nullExp);
  }

  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return Utils.getNearestSkippableStatement(nullExp);
  }

  public List<PatchTemplate> generate(CtExpression<?> nullExp) {
    final CtElement skipFrom = this.createSkipFrom(nullExp);
    final CtElement skipTo = this.createSkipTo(nullExp);
    final List<CtElement> nullBlockStmts = createNullBlockStmts(nullExp);
    return nullBlockStmts.stream()
        .map(s -> new PatchTemplateSynth(getPatchID(skipFrom, skipTo), nullExp, s, skipFrom, skipTo))
        .collect(Collectors.toList());
  }

  abstract List<CtElement> createNullBlockStmts(CtExpression<?> nullExp);
}