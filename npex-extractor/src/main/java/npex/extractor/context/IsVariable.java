package npex.extractor.context;

import spoon.reflect.declaration.CtVariable;

public class IsVariable extends AbstractVariableTypeContext<CtVariable> {
  public IsVariable() {
    super(CtVariable.class);
  }
}