package npex.buggycode;

import npex.Utils;
import spoon.javadoc.internal.Pair;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtClass;

public class NullHandleIf extends NullHandle {
  Pair<CtBlock<?>, CtBlock<?>> branches;

  public NullHandleIf(CtIf handle) throws IllegalArgumentException {
    super(handle, handle.getCondition());
    this.branches = new Pair<>(handle.getThenStatement(), handle.getElseStatement());
  }

  public CtBlock<?> getNullBlock() {
    return isCondKindEqual() ? branches.a : branches.b;
  }

  public CtBlock<?> getNonNullBlock() {
    CtBlock<?> blk = !isCondKindEqual() ? branches.a : branches.b;
    if (blk != null)
      return blk;

    if (this.isNullReturn()) {
      int idx = this.parentBlock.getStatements().indexOf(this.handle);
      CtBlock<?> nonNullBlock = this.parentBlock.getFactory().createBlock();
      this.parentBlock.getStatements().stream().skip(idx + 1).forEach(x -> nonNullBlock.addStatement(x.clone()));
      return nonNullBlock;
    }
    return blk;
  }

  private boolean isNullReturn() {
    if (this.getNullBlock() != null)
      return this.getNullBlock().getStatements().stream().anyMatch(x -> x instanceof CtReturn<?>);
    return false;
  }

  public void stripNullHandle(CtClass<?> klass) throws ArrayIndexOutOfBoundsException {
    boolean isElseBlockEmpty = branches.b != null ? branches.b.getStatements().isEmpty() : true;
    int pos = parentBlock.getStatements().indexOf(handle);
    CtBlock<?> blk = Utils.findMatchedElement(klass, parentBlock);
    if (isNullReturn() && isElseBlockEmpty) {
      blk.removeStatement(blk.getStatement(pos));
    } else {
      blk.getStatement(pos).replace(getNonNullBlock());
    }
  }
}