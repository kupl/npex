package npex.extractor.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.filters.MethodOrConstructorFilter;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;

public abstract class AbstractCallerMethodContext implements Context {
  final static Logger logger = LoggerFactory.getLogger(AbstractVariableTypeContext.class);

  public Boolean extract(CtInvocation invo, int nullPos) {
    CtExecutable exec = invo.getParent(new MethodOrConstructorFilter());
    return exec != null && predicateOnMethod(exec);
  }

  protected abstract boolean predicateOnMethod(CtExecutable callee);

}