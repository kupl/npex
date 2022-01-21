package npex.extractor.context;

import spoon.reflect.declaration.CtParameter;

public class IsParameter extends AbstractVariableTypeContext<CtParameter> {
  public IsParameter() {
    super(CtParameter.class);
  }
}