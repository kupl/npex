package npex.errortracer;

import java.io.File;

import spoon.SpoonException;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLambda;
import spoon.reflect.code.CtStatement;

public class InvocationLoggerProcessor extends AbstractLoggerProcessor<CtInvocation<?>> {
  public InvocationLoggerProcessor(File projectRoot) {
    super(projectRoot, "CALLSITE");
  }

  boolean handleSingleBlockLambda(CtStatement s, CtStatement snippet) {
    if (!(s.getParent() instanceof CtLambda))
      return false;
    CtLambda<?> lambdaOrg = s.getParent(CtLambda.class);
    CtBlock<?> blk = getFactory().createBlock();
    CtLambda<?> lambda = getFactory().createLambda();

    blk.addStatement(snippet);
    blk.addStatement((CtStatement) lambdaOrg.getExpression().clone());
    lambda.setBody(blk);
    lambdaOrg.replace(lambda);
    return true;
  }

  @Override
  public void process(CtInvocation<?> e) {
    CtStatement tracer = createPrintStatement(e);
    CtStatement enclStmt = npex.Utils.getEnclosingStatement(e);

    try {
      enclStmt.insertBefore(tracer);
    } catch (SpoonException exn) {
      if (handleSingleBlockLambda(enclStmt, tracer)) {
        return;
      }
      if (exn.getLocalizedMessage().contains("before a super or this")) {
        return;
      }
      logger.fatal(exn.getLocalizedMessage());
      logger.fatal(e.toString());
      throw exn;
    }
  }

  String getElementName(CtInvocation<?> e) {
    return e.getExecutable().getSimpleName();
  }

  boolean _isToBeProcessed(CtInvocation<?> e) {
    return !e.getExecutable().isConstructor();
  }

}