package npex.extractor.context;

import spoon.reflect.declaration.CtField;

public class IsField extends AbstractVariableTypeContext<CtField> {
  public IsField() {
    super(CtField.class);
  }

}