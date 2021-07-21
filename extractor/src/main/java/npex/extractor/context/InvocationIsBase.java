package npex.extractor.context;

import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.path.CtRole;

public class InvocationIsBase implements Context {

  public Boolean extract(CtAbstractInvocation invo, int nullPos) {
    return invo.getRoleInParent().equals(CtRole.TARGET);
  }
}