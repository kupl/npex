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

import java.util.List;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import npex.synthesizer.buggycode.BuggyCode;
import npex.synthesizer.buggycode.NullHandleTernary;
import npex.synthesizer.template.PatchTemplateDeveloper;
import spoon.reflect.code.CtExpression;

public class BuggyCodeTest {
  protected Logger logger = Logger.getLogger(BuggyCodeTest.class);

  protected List<BuggyCode> buggyCodes;

  @Before
  public void setup() {
    PatchSynthesizer synthesizer = new PatchSynthesizer(
        "/media/4tb/npex/npex_data/benchmarks/sling-org-apache-sling-tracer/");
    this.buggyCodes = synthesizer.extractBuggyCodes();
  }

  protected void testWithBuggy(Consumer<BuggyCode> consumer) {
    buggyCodes.forEach(consumer);
  }

  private Consumer<BuggyCode> generateBuggyBlockConsumer = buggy -> {
    CtExpression<?> nullPointer = buggy.getNullPointer();
    logger.info("Null Handle Type: " + buggy.getNullHandle().getClass());
    logger.info("Null Pointer: " + nullPointer);
    logger.info("Original Block: ");
    logger.info(buggy.getOriginalBlock());
    logger.info("Buggy Block: ");
    logger.info(buggy.getBuggyBlock());
    logger.info(buggy.getNPEInfo().toString());
  };

  @Test
  public void testBuggyCodes() {
    testWithBuggy(generateBuggyBlockConsumer);
  };

  @Test
  public void testDeveloperPatches() {
    testWithBuggy(generateBuggyBlockConsumer.andThen(buggy -> {
      PatchTemplateDeveloper template = buggy.generateDeveloperPatch();
      template.apply();
      logger.info("Patched Block");
      logger.info(template.getBlock());
    }));
  }

  @Test
  public void testPrintNullExp() {
    testWithBuggy(buggy -> {
      CtExpression<?> nullPointer = buggy.getNullPointer();
      logger.info(buggy.getID());
      logger.info(nullPointer);
      logger.info(nullPointer.getParent(CtExpression.class));
      logger.info(buggy.getOriginalBlock());
      logger.info(buggy.getBuggyBlock());
    });

  }

  @Test
  public void testBuggyCodesFromTernaries() {
    this.buggyCodes.stream().filter(buggy -> (buggy.getNullHandle() instanceof NullHandleTernary))
        .forEach(generateBuggyBlockConsumer);
  }
}
