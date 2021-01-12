package npex.synthesizer.template;

import java.util.List;

import npex.synthesizer.Utils;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtExecutable;

public class PatchTemplateIf extends PatchTemplate {
  private enum SkipKind {
    SKIPONLY, DOSMTH
  };

  private final CtStatement nullExecStmt;
  private final CtStatement skipFrom, skipTo;
  private final SkipKind kind;

  public PatchTemplateIf(String id, CtExpression nullExp, CtStatement nullExecStmt, CtStatement skipFrom,
      CtStatement skipTo) {
    super(id, nullExp);
    this.nullExecStmt = nullExecStmt;
    this.skipFrom = Utils.findMatchedElementLookParent(skipFrom, ast);
    this.skipTo = Utils.findMatchedElementLookParent(skipTo, ast);
    this.kind = (nullExecStmt == null) ? SkipKind.SKIPONLY : SkipKind.DOSMTH;
  }

  protected CtExecutable implement() {
    CtIf ifStmt = factory.createIf();
    CtBlock<?> thenBlock = factory.createBlock();

    CtBlock<?> skipBlock = skipFrom.getParent(CtBlock.class);
    List<CtStatement> stmts = skipBlock.getStatements();
    switch (kind) {
      case SKIPONLY:
        ifStmt.setCondition(createNullCond(false));
        int idxFrom = stmts.indexOf(skipFrom);
        int idxTo = stmts.indexOf(skipTo);
        for (CtStatement s : stmts.subList(idxFrom, idxTo + 1)) {
          skipBlock.removeStatement(s);
          thenBlock.addStatement(s);
        }
        skipBlock.addStatement(idxFrom, ifStmt);
        break;
      case DOSMTH:
        ifStmt.setCondition(createNullCond(true));
        thenBlock.addStatement(nullExecStmt);
        break;
    }

    ifStmt.setThenStatement(thenBlock);
    return ast;
  }
}