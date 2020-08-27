package npex.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

public class SkipReturnStrategy extends SkipStrategy {
  public SkipReturnStrategy() {
    this.name = "SkipReturn";
  }

  @Override
  public boolean isApplicable(CtExpression<?> nullExp) {
    CtMethod<?> sinkMethod = nullExp.getParent(CtMethod.class);
    /* Sink in the constructor */
    if (sinkMethod == null) {
      return false;
    }
    try {
      System.out.println(DefaultValueTable.getValueString(sinkMethod.getType()));
    } catch (NotImplementedException e) {
      System.out.println(e);
      return false;
    }
    return true;
  }

  @Override
  protected CtElement createSkipTo(CtExpression<?> nullExp) {
    return nullExp.getParent(CtMethod.class);
  }

  protected <R> CtReturn<R> createReturnStmt(CtMethod<R> sinkMethod) {
    Factory factory = sinkMethod.getFactory();
    CtReturn<R> retStmt = factory.createReturn();
    final CtTypeReference<R> retTyp = sinkMethod.getType();
    if (retTyp.getSimpleName() == "void") {
      return retStmt;
    }

    CtCodeSnippetExpression<R> exprSnippet = factory.createCodeSnippetExpression();
    exprSnippet.setValue(String.format(DefaultValueTable.getValueString(retTyp)));

    return retStmt.setReturnedExpression(exprSnippet.compile());
  }

  @Override
  protected List<CtElement> createNullBlockStmts(CtExpression<?> nullExp) {
    ArrayList<CtElement> ret = new ArrayList<>();
    ret.add(createReturnStmt(nullExp.getParent(CtMethod.class)));
    return ret;
  }
}