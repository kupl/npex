package npex.template;

import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtMethod;

public interface PatchTemplate {
  String getID();

  CtBlock<?> getBlock();

  CtMethod<?> apply();

  SourceChange<?> getSourceChange();
}