package npex.extractor.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtExecutable;

public abstract class AbstractCalleeMethodContext implements Context {
  final static Logger logger = LoggerFactory.getLogger(AbstractCalleeMethodContext.class);

  public Boolean extract(CtAbstractInvocation invo, int nullpos) {
    CtExecutable exec = invo.getExecutable().getExecutableDeclaration();
    return exec != null ? predicateOnMethod(exec) : false;
  }

  protected abstract boolean predicateOnMethod(CtExecutable exec);

}