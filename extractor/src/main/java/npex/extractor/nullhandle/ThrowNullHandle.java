package npex.extractor.nullhandle;

import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtThrow;

public class ThrowNullHandle extends AbstractNullHandle {
  private final CtBlock parentBlock;
  private CtExpression<? extends Throwable> thrownExn;

  public ThrowNullHandle(CtIf handle, CtBinaryOperator nullCond, CtThrow thrownStmt) {
    super(handle, nullCond);
    this.parentBlock = handle.getParent(CtBlock.class);
    this.thrownExn = thrownStmt.getThrownExpression();
  }

  @Override
  protected AbstractNullModelScanner createNullModelScanner(CtExpression nullExp) {
    return new NullModelScanner(nullExp);
  }

  @Override
  public void collectModels() {
    AbstractNullModelScanner scanner = createNullModelScanner(nullExp);
    parentBlock.accept(scanner);
    this.models = scanner.getResult();
  }

  private class NullModelScanner extends AbstractNullModelScanner {
    private boolean handleVisited = false;

    public NullModelScanner(CtExpression nullExp) {
      super(nullExp);
    }

    public void visitCtBinaryOperator(CtBinaryOperator binop) {
      super.visitCtBinaryOperator(binop);
      handleVisited = binop.equals(nullCond) && binop.getPosition().equals(nullCond.getPosition());
    }

    public void visitCtInvocation(CtInvocation invo) {
      if (!handleVisited) {
        return;
      }
      super.visitCtInvocation(invo);

      if (!isTargetInvocation(invo))
        return;

      models.add(new ThrowNullModel(nullExp, invo, thrownExn));
      terminate();
    }

    public void visitCtAssignment(CtAssignment assignment) {
      super.visitCtAssignment(assignment);
      if (assignment.getAssigned().equals(nullExp)) {
        logger.error("{} has been re-defined between handle and invocation!: {} at location {}", nullExp, assignment,
            assignment.getPosition());
        terminate();
      }
    }

  }
}