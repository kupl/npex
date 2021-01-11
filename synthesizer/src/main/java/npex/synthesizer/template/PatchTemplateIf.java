package npex.synthesizer.template;

import java.util.List;

import npex.synthesizer.Utils;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtExecutable;

public class PatchTemplateIf extends PatchTemplate2 {
  private enum SkipKind {
    PLAIN, FLOWBREAK
  };

  private final CtCFlowBreak flowBreak;
  private final CtStatement skipFrom, skipTo;
  private final SkipKind kind;

  public PatchTemplateIf(String id, CtExpression nullExp, CtCFlowBreak flowBreak, CtStatement skipFrom,
      CtStatement skipTo) {
    super(id, nullExp);
    this.flowBreak = flowBreak;
    this.skipFrom = Utils.findMatchedElementLookParent(skipFrom, ast);
    this.skipTo = Utils.findMatchedElementLookParent(skipTo, ast);
    this.kind = (flowBreak == null) ? SkipKind.PLAIN : SkipKind.FLOWBREAK;
  }

  protected CtExecutable implement() {
    CtIf ifStmt = factory.createIf();
    CtBlock<?> thenBlock = factory.createBlock();

    CtBlock<?> skipBlock = skipFrom.getParent(CtBlock.class);
    List<CtStatement> stmts = skipBlock.getStatements();
    switch (kind) {
      case PLAIN:
        ifStmt.setCondition(createNullCond(false));
        int idxFrom = stmts.indexOf(skipFrom);
        int idxTo = stmts.indexOf(skipTo);
        for (CtStatement s : stmts.subList(idxFrom, idxTo + 1)) {
          skipBlock.removeStatement(s);
          thenBlock.addStatement(s);
        }
        skipBlock.addStatement(idxFrom, ifStmt);
        break;
      case FLOWBREAK:
        ifStmt.setCondition(createNullCond(true));
        thenBlock.addStatement(flowBreak);
        break;
    }

    ifStmt.setThenStatement(thenBlock);
    return ast;
  }
}