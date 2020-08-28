package npex.buggycode;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import npex.NPEInfo;
import npex.Utils;
import npex.template.PatchTemplateDeveloper;
import npex.template.SourceChange;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;

public class BuggyCode {
  private CtClass<?> klass;
  final private CtBlock<?> buggyBlock;
  final private CtBlock<?> orgBlock;
  final private File orgSourceFile;
  final private NullHandle nullHandle;
  final private File projectDir;

  static Logger logger = Logger.getLogger(BuggyCode.class);

  public BuggyCode(String projectPath, NullHandle nullHandle) throws IOException {
    this.projectDir = new File(projectPath);
    this.nullHandle = nullHandle;
    try {
      this.klass = nullHandle.getStatement().getParent(CtClass.class).clone();
    } catch (Exception e) {
      this.klass = nullHandle.getNullPointer().getParent(CtClass.class).clone();
    }
    this.orgBlock = nullHandle.getStatement().getParent(CtBlock.class);
    this.buggyBlock = Utils.findMatchedElement(this.klass, orgBlock);
    this.orgSourceFile = Utils.getSourceFile(nullHandle.getNullPointer());
  }

  public CtExpression<?> getNullPointer() throws NoSuchElementException {
    CtBlock<?> blk;
    if (nullHandle instanceof NullHandleIf) {
      blk = Utils.findMatchedElement(this.klass, ((NullHandleIf) nullHandle).getNonNullBlock());
    } else {
      blk = buggyBlock;
    }
    CtExpression<?> found = Utils.findMatchedElement(blk, nullHandle.getNullPointer());
    return found;
  }

  public NullHandle getNullHandle() {
    return this.nullHandle;
  }

  public BuggyCode stripNullHandle() throws ArrayIndexOutOfBoundsException {
    try {
      nullHandle.stripNullHandle(klass);
      return this;
    } catch (ArrayIndexOutOfBoundsException e) {
      logger.fatal("Could not strip nullhandle (ArrayIndexOutOfBounds): " + nullHandle.toString());
      return null;
    } catch (NoSuchElementException e) {
      logger.fatal("Could not strip nullhandle (NoSuchElement): " + nullHandle.toString());
      return null;
    }
  }

  public String getID() {
    String sourceFileName = this.orgSourceFile.getName().split("[.]")[0];
    return String.format("%s_%d", sourceFileName, this.nullHandle.getStatement().getPosition().getLine());
  }

  public boolean isBugInConstructor() {
    return this.buggyBlock.getParent(CtMethod.class) == null && this.buggyBlock.getParent(CtConstructor.class) != null;
  }

  public boolean hasNullPointerIdentifiable() {
    try {
      this.getNullPointer();
      return true;
    } catch (NoSuchElementException e) {
      return false;
    }
  }

  public boolean isAccessPathResolved() {
    try {
      this.klass.toString();
      return true;
    } catch (spoon.SpoonException e) {
      return false;
    }
  }

  public SourceChange<?> getSourceChange() {
    CtMethod<?> parentMethod = this.orgBlock.getParent(CtMethod.class);
    Class<? extends CtElement> klass = parentMethod != null ? CtMethod.class : CtConstructor.class;
    return new SourceChange<>(this.orgBlock.getParent(klass), this.buggyBlock.getParent(klass),
        this.nullHandle.getStatement());
  }

  public PatchTemplateDeveloper generateDeveloperPatch() {
    return new PatchTemplateDeveloper(this.getNullPointer(), this.nullHandle);
  }

  public CtBlock<?> getOriginalBlock() {
    return this.orgBlock;
  }

  public CtBlock<?> getBuggyBlock() {
    return this.buggyBlock;
  }

  public NPEInfo getNPEInfo() {
    String filepath = projectDir.toURI().relativize(orgSourceFile.toURI()).getPath();
    CtExpression<?> nullPointer = getNullPointer();
    int line = nullPointer.getPosition().getLine();
    String deref_field = NPEInfo.resolveDerefField(nullPointer);
    CtMethod<?> sink_method = buggyBlock.getParent(CtMethod.class);
    CtClass<?> sink_class = klass;
    return new NPEInfo(filepath, line, deref_field, sink_method, sink_class);
  }

}