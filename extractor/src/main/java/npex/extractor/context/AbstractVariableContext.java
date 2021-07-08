package npex.extractor.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;

public abstract class AbstractVariableContext extends Context {
  final static Logger logger = LoggerFactory.getLogger(AbstractVariableTypeContext.class);

  public Boolean extract(CtAbstractInvocation invo, int nullPos) {
    CtExpression nullExp = nullPos == -1 && invo instanceof CtInvocation i ? i.getTarget()
        : (CtExpression) invo.getArguments().get(nullPos);
    return nullExp instanceof CtVariableAccess va && va.getVariable() != null
        && va.getVariable().getDeclaration() != null && extract(va);
  }

  protected abstract Boolean extract(CtVariableAccess va);

}