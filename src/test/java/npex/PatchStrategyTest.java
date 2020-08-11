package npex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import npex.buggycode.BuggyCode;
import npex.strategy.InitPointerStrategy;
import npex.strategy.ObjectInitializer;
import npex.strategy.PatchStrategy;
import npex.strategy.ReplacePointerStrategy;
import npex.strategy.ReplaceSinkExprStrategy;
import npex.strategy.SkipBlockStrategy;
import npex.strategy.SkipBreakStrategy;
import npex.strategy.SkipContinueStrategy;
import npex.strategy.SkipReturnStrategy;
import npex.strategy.SkipSinkStatementStrategy;
import npex.strategy.VarInitializer;
import npex.template.PatchTemplate;
import spoon.reflect.code.CtExpression;

public class PatchStrategyTest extends BuggyCodeTest {
  protected List<PatchStrategy> strategies = new ArrayList<PatchStrategy>();

  @Before
  public void setup() {
    super.setup();
    strategies.add(new SkipSinkStatementStrategy());
    strategies.add(new SkipBreakStrategy());
    strategies.add(new SkipContinueStrategy());
    strategies.add(new SkipReturnStrategy());
    strategies.add(new SkipBlockStrategy());
  }

  protected List<PatchTemplate> generatePatchTemplates(BuggyCode buggy) {
    CtExpression<?> nullExp = buggy.getNullPointer();
    return this.strategies.stream().filter(stgy -> stgy.isApplicable(nullExp)).map(stgy -> stgy.generate(nullExp))
        .collect(Collectors.toList());
  }

  public void testStrategy(PatchStrategy strategy) {
    testWithBuggy(buggy -> {
      CtExpression<?> nullExp = buggy.getNullPointer();
      if (strategy.isApplicable(nullExp)) {
        logger.info(String.format("Strategy: %s is Applicable!", strategy.getName()));
        PatchTemplate template = strategy.generate(nullExp);
        template.apply();
        logger.info("Patch by " + strategy.getName());
        logger.info(template.getBlock());
      } else {
        logger.info(strategy.getName() + " is not applicable!");
      }
    });
  }

  @Test
  public void testAll() {
    this.strategies.forEach(strategy -> testStrategy(strategy));
  }

  @Test
  public void testInitVarStrategy() {
    testStrategy(new InitPointerStrategy(new VarInitializer()));
  }

  @Test
  public void testInitObjStrategy() {
    testStrategy(new InitPointerStrategy(new ObjectInitializer()));
  }

  @Test
  public void testReplacePointerVar() {
    testStrategy(new ReplacePointerStrategy(new VarInitializer()));
  }

  @Test
  public void testReplacePointerObj() {
    testStrategy(new ReplacePointerStrategy(new ObjectInitializer()));
  }

  @Test
  public void testReplaceSinkExprVar() {
    testStrategy(new ReplaceSinkExprStrategy(new VarInitializer()));
  }

  @Test
  public void testReplaceSinkExprObj() {
    testStrategy(new ReplaceSinkExprStrategy(new ObjectInitializer()));
  }

  @Test
  public void testReplaceEntirerExprVar() {
    /* TODO */
    // testStrategy(new ReplacePointerStrategy(new VarInitializer()));
  }
}