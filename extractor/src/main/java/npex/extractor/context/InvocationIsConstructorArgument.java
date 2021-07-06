package npex.extractor.context;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.path.CtRole;

public class InvocationIsConstructorArgument extends Context {

  public Boolean extract(CtAbstractInvocation invo, int nullPos) {
    return invo.getRoleInParent().equals(CtRole.ARGUMENT) && invo.getParent(CtConstructorCall.class) != null;
  }
}