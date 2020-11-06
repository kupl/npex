package npex.synthesizer.template;

import java.io.File;
import java.io.IOException;

import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtNamedElement;

public interface PatchTemplate {
  String getID();

  CtBlock<?> getBlock();

  CtNamedElement apply();

  SourceChange<?> getSourceChange();

  default void store(String projectRootPath, File outputDir) throws IOException {
    try {
      getSourceChange().store(projectRootPath, outputDir);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }
}