package npex.extractor.context;

import spoon.reflect.code.CtVariableAccess;

public class VariableIsFinal extends AbstractVariableContext {
  @Override
  protected Boolean extract(CtVariableAccess va) {
    return va.getVariable().getDeclaration().isFinal();
  }
}