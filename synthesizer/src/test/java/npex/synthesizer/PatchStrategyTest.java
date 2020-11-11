/**
 * The MIT License
 *
 * Copyright (c) 2020 Software Analysis Laboratory, Korea University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package npex.synthesizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import npex.synthesizer.buggycode.BuggyCode;
import npex.synthesizer.strategy.InitPointerStrategy;
import npex.synthesizer.strategy.ObjectInitializer;
import npex.synthesizer.strategy.PatchStrategy;
import npex.synthesizer.strategy.ReplaceEntireExpressionStrategy;
import npex.synthesizer.strategy.ReplacePointerStrategy;
import npex.synthesizer.strategy.SkipBlockStrategy;
import npex.synthesizer.strategy.SkipBreakStrategy;
import npex.synthesizer.strategy.SkipContinueStrategy;
import npex.synthesizer.strategy.SkipReturnStrategy;
import npex.synthesizer.strategy.SkipSinkStatementStrategy;
import npex.synthesizer.strategy.VarInitializer;
import npex.synthesizer.template.PatchTemplate;
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