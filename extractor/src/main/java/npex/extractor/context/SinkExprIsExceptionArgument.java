package npex.extractor.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.visitor.filter.TypeFilter;

public class SinkExprIsExceptionArgument implements Context {
  static Logger logger = LoggerFactory.getLogger(SinkExprIsExceptionArgument.class);

  public Boolean extract(CtAbstractInvocation invo, int nullPos) {
    return invo.getParent(new TypeFilter<CtConstructorCall>(CtConstructorCall.class) {
      @Override
      public boolean matches(CtConstructorCall e) {
        return e.getType().isSubtypeOf(invo.getFactory().createCtTypeReference(Throwable.class));
      }
    }) != null;
  }

}