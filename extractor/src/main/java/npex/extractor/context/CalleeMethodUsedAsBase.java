package npex.extractor.context;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.filter.TypeFilter;

public class CalleeMethodUsedAsBase extends AbstractCalleeMethodContext {

  @Override
  public Boolean extract(CtExecutable callee, int nullPos) {
    String calleeSignature = callee.getSignature();
    TypeFilter<CtInvocation> filter = new TypeFilter<>(CtInvocation.class){
      @Override 
      public boolean matches(CtInvocation invo) {
        return invo.getExecutable().getSignature().equals(calleeSignature)
            && invo.getRoleInParent().equals(CtRole.TARGET);
      }
    };

    CtClass klass = callee.getParent(CtClass.class);
    return klass != null && !klass.getElements(filter).isEmpty();
  }
}