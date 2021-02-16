package npex.extractor.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtVariable;

public abstract class AbstractVariableTypeContext<T extends CtVariable> implements Context {
  final static Logger logger = LoggerFactory.getLogger(AbstractVariableTypeContext.class);
  final protected Class<T> type;

  public AbstractVariableTypeContext(Class<T> type) {
    this.type = type;
  }

  public Boolean extract(CtInvocation invo, int nullPos) {
    CtExpression nullExp = nullPos == -1 ? invo.getTarget() : (CtExpression) invo.getArguments().get(nullPos);
    return nullExp instanceof CtVariableAccess va
        && type.isAssignableFrom(va.getVariable().getDeclaration().getClass());
  }

}