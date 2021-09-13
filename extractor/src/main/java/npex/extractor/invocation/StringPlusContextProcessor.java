package npex.extractor.invocation;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.utils.TypeUtil;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;

public class StringPlusContextProcessor extends AbstractProcessor<CtBinaryOperator> {
  static Logger logger = LoggerFactory.getLogger(StringPlusContextProcessor.class);
  static private final CtExecutableReference stringBuilderAppend = TypeUtil.STRING_BUILDER.getTypeDeclaration().getMethod("append",
      TypeUtil.STRING).getReference();

  private Set<String> traceClasses;

  public StringPlusContextProcessor(Set<String> traceClasses) throws IOException {
    super();
    this.traceClasses = traceClasses;
  }

  @Override
  public boolean isToBeProcessed(CtBinaryOperator bo) {
    CtClass klass = bo.getParent(CtClass.class);
    if (!traceClasses.isEmpty() && klass != null && !traceClasses.contains(klass.getQualifiedName()))
      return false;
    if (!bo.getPosition().isValidPosition()) {
      return false;
    }

    return BinaryOperatorKind.PLUS.equals(bo.getKind()) && TypeUtil.STRING.equals(bo.getLeftHandOperand().getType())
        && TypeUtil.STRING.equals(bo.getRightHandOperand().getType());
  }

  @Override
  public void process(CtBinaryOperator bo) {
    Factory f = bo.getFactory();
    CtExpression lhs = bo.getLeftHandOperand();
    CtExpression rhs = bo.getRightHandOperand();
    CtConstructorCall base = f.createConstructorCall(TypeUtil.STRING_BUILDER);
    CtInvocation invo = f.createInvocation(base, stringBuilderAppend, rhs);
    CtElement par = bo.getParent();
    invo.setPosition(bo.getPosition());
    bo.replace(invo);
    invo.setParent(par);
  }
}
