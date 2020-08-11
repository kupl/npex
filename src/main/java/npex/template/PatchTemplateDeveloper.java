package npex.template;

import npex.Utils;
import npex.buggycode.NullHandle;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

public class PatchTemplateDeveloper implements PatchTemplate {
  final CtStatement handleStmt;
  CtMethod<?> buggyMethod;
  NullHandle nullHandle;

  public PatchTemplateDeveloper(CtExpression<?> nullExp, NullHandle nullHandle) {
    this.handleStmt = nullHandle.getStatement();
    this.nullHandle = nullHandle;
    this.buggyMethod = Utils.findMatchedElement(handleStmt.getParent(CtClass.class), handleStmt)
        .getParent(CtMethod.class);
  }

  public CtMethod<?> apply() {
    return this.nullHandle.getStatement().getParent(CtMethod.class);
  }

  public CtBlock<?> getBlock() {
    return handleStmt.getParent(CtBlock.class);

  }

  public String getID() {
    return "devel_patch";
  }

  public SourceChange<CtMethod<?>> getSourceChange() {
    return new SourceChange<>(this.buggyMethod, this.apply(), this.handleStmt);
  }
}