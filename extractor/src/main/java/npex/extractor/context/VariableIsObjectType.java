package npex.extractor.context;

import spoon.reflect.code.CtVariableAccess;

public class VariableIsObjectType extends AbstractVariableContext {
  @Override
  protected Boolean extract(CtVariableAccess va) {
    return va.getType() != null && va.getType().getQualifiedName().equals("java.lang.Object");
  }
}