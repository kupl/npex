package npex.template;

import java.util.List;

import npex.Utils;
import spoon.SpoonException;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.path.CtRole;

public class SkipRange {
  public final CtElement from;
  public final CtElement to;
  public final Kind kind;

  enum Kind {
    Loop, Return, Break, Continue, Nothing, Normal
  }

  private static Kind getKind(CtElement from, CtElement to) {
    if (from instanceof CtLoop && to instanceof CtLoop)
      return Kind.Loop;
    if (to instanceof CtMethod<?>)
      return Kind.Return;
    else if (to instanceof CtLoop)
      return Kind.Break;
    else if (to == null)
      return Kind.Nothing;

    CtLoop enclosingLoop = to.getParent(CtLoop.class);
    if (enclosingLoop != null) {
      CtBlock<?> loopBody = (CtBlock<?>) enclosingLoop.getBody();
      if (to.equals(loopBody.getLastStatement())) {
        return Kind.Continue;
      }
    }
    return Kind.Normal;
  }

  public SkipRange(CtClass<?> targetClass, CtElement from, CtElement to) {
    this.from = Utils.findMatchedElement(targetClass, from);
    this.to = (to != null) ? Utils.findMatchedElement(targetClass, to) : null;
    this.kind = getKind(from, to);
  }

  public CtStatement getSkipFromStmt() {
    return (from instanceof CtStatement) ? (CtStatement) from : from.getParent(CtStatement.class);
  }

  public void replaceSkipRange(CtIf ifStmt) throws IllegalArgumentException, SpoonException {
    if (!(kind == Kind.Normal || kind == Kind.Loop))
      throw new IllegalArgumentException();

    if (from.equals(to) && from instanceof CtBlock) {
      replaceSkipRange(ifStmt, (CtBlock<?>) from);
      return;
    }

    CtBlock<?> skipBlock = this.from.getParent(CtBlock.class);
    List<CtStatement> stmts = skipBlock.getStatements();
    int idxFrom = stmts.indexOf(this.from);
    int idxTo = stmts.indexOf(this.to);

    /* skip range should be within the same block */
    assert (skipBlock.equals(this.to.getParent(CtBlock.class)));
    assert (idxFrom > -1 && idxTo > -1);
    CtBlock<?> thenBlock = ifStmt.getFactory().createBlock();
    ifStmt.setThenStatement(thenBlock);
    for (CtStatement stmt : stmts.subList(idxFrom, idxTo + 1)) {
      skipBlock.removeStatement(stmt);
      thenBlock.addStatement(stmt);
    }
    skipBlock.addStatement(idxFrom, ifStmt);
  }

  private void replaceSkipRange(CtIf ifStmt, CtBlock<?> block) {
    CtBlock<?> thenBlock = ifStmt.getFactory().createBlock();
    ifStmt.setThenStatement(thenBlock);
    thenBlock.addStatement(block.clone());

    // For the case where explicit block parentheses are needed, e.g., a lambda body
    if (block.getRoleInParent().equals(CtRole.BODY)) {
      CtBlock<?> wrappingBlock = ifStmt.getFactory().createBlock();
      wrappingBlock.addStatement(ifStmt);
      block.replace(wrappingBlock);
      return;
    }

    block.replace(ifStmt);
  }
}