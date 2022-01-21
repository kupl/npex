package npex.extractor.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtVariable;

public abstract class AbstractVariableTypeContext<T extends CtVariable> extends AbstractVariableContext {
  final static Logger logger = LoggerFactory.getLogger(AbstractVariableTypeContext.class);
  final protected Class<T> type;

  public AbstractVariableTypeContext(Class<T> type) {
    this.type = type;
  }

  @Override
  protected Boolean extract(CtVariableAccess va) {
    return type.isAssignableFrom(va.getVariable().getDeclaration().getClass());
  }
}