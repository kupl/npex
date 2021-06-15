package npex.extractor.nullhandle;

import npex.common.NPEXException;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;

public class ThrowNullModel extends NullModel {
  public ThrowNullModel(CtExpression nullExp, CtElement sinkBody, CtExpression nullValue) {
    super(nullExp, sinkBody, nullValue, true);
  }

  @Override
  protected String abstractNullValue(CtExpression nullValue) throws NPEXException {
    if (nullValue.getType() == null) {
      throw new NPEXException(String.format("Cannot extract null values for model %s: its value type is null", this));
    }
    return nullValue.getType().getSimpleName();
  }

}