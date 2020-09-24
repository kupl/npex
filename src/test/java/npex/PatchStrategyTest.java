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
import npex.strategy.ReplaceEntireExpressionStrategy;
import npex.strategy.ReplacePointerStrategy;
import npex.strategy.SkipBlockStrategy;
import npex.strategy.SkipBreakStrategy;
import npex.strategy.SkipContinueStrategy;
import npex.strategy.SkipReturnStrategy;
import npex.strategy.SkipSinkStatementStrategy;
import npex.strategy.VarInitializer;
import npex.template.PatchTemplate;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtClass;

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
    return this.strategies.stream().filter(stgy -> stgy.isApplicable(nullExp))
        .flatMap(stgy -> stgy.generate(nullExp).stream()).collect(Collectors.toList());
  }

  void testStrategy(PatchStrategy strategy) {
    testWithBuggy(buggy -> {
      CtExpression<?> nullExp = buggy.getNullPointer();
      if (strategy.isApplicable(nullExp)) {
        logger.info(String.format("Strategy: %s is Applicable!", strategy.getName()));
        logger.info("Type of NullExp " + nullExp.getClass().toString());
        logger.info("Parent class of NullExp " + nullExp.getParent(CtClass.class).getSimpleName());
        List<PatchTemplate> generated = strategy.generate(nullExp);
        logger.info(String.format("-- %d templates generated.", generated.size()));
        generated.forEach(t -> {
          t.apply();
          logger.info("Patch by " + strategy.getName());
          logger.info(t.getBlock());
        });
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
  public void testReplaceEntirerExprVar() {
    testStrategy(new ReplaceEntireExpressionStrategy(new VarInitializer()));
    testStrategy(new ReplaceEntireExpressionStrategy(new ObjectInitializer()));
  }

}