package npex.extractor.context;

import npex.common.utils.TypeUtil;
import spoon.reflect.code.CtVariableAccess;

public class VariableIsObjectType extends AbstractVariableContext {
  @Override
  protected Boolean extract(CtVariableAccess va) {
    return va.getType() != null && va.getType().equals(TypeUtil.OBJECT);
  }
}