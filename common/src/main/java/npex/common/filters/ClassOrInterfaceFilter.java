package npex.common.filters;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.filter.AbstractFilter;

public class ClassOrInterfaceFilter extends AbstractFilter<CtType> {
  public boolean matches(CtType e) {
    return e instanceof CtClass || e instanceof CtInterface;
  }

}