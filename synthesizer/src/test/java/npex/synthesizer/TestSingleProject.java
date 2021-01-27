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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.synthesizer.strategy.InitPointerStrategy;
import npex.synthesizer.strategy.ObjectInitializer;
import npex.synthesizer.strategy.PatchStrategy;
import npex.synthesizer.strategy.PrimitiveInitializer;
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

public class TestSingleProject {
  List<PatchStrategy> strategies = Arrays.asList(new PatchStrategy[] { new SkipBreakStrategy(),
      new SkipContinueStrategy(), new SkipSinkStatementStrategy(), new SkipReturnStrategy(), new SkipBlockStrategy(),
      new InitPointerStrategy(new VarInitializer()), new InitPointerStrategy(new ObjectInitializer()),
      new ReplacePointerStrategy(new VarInitializer()), new ReplacePointerStrategy(new ObjectInitializer()),
      new ReplacePointerStrategy(new PrimitiveInitializer()), new ReplaceEntireExpressionStrategy(new VarInitializer()),
      new ReplaceEntireExpressionStrategy(new PrimitiveInitializer()),
      new ReplaceEntireExpressionStrategy(new ObjectInitializer()) });

  final static protected Logger logger = LoggerFactory.getLogger(TestSingleProject.class);

  @Test
  public void test() {
    String projectID = "Lang-33";
    // String NPEXDataPath = "/media/4tb/npex/NPEX_DATA";
    String NPEXDataPath = "/media/4tb/npex/originals/benchmarks-commits/benchmarks-defects4j/";
    String projectPath = String.format("%s/%s-buggy/", NPEXDataPath, projectID);
    String npePath = String.format("%s/npe.json", projectPath);

    PatchSynthesizer mvn = new PatchSynthesizer(projectPath, strategies);
    List<PatchTemplate> templates = new ArrayList<>();
    try {
      CtExpression<?> nullExp = NPEInfo.readFromJSON(mvn.getFactory(), npePath).resolve();
      System.out.println("NPE Expression resolved: " + nullExp);
      for (PatchStrategy stgy : strategies) {
        if (stgy.isApplicable(nullExp)) {
          System.out.println(String.format("Strategy %s is applicable!", stgy.getName()));
          List<PatchTemplate> generated = stgy.enumerate(nullExp);
          System.out.println(String.format("-- %d templates generated.", generated.size()));
          templates.addAll(generated);
        } else
          System.out.println(String.format("Strategy %s is not applicable!", stgy.getName()));
      }
    } catch (IOException e) {
      System.out.println("Could not open npe.json");
      return;
    } catch (NoSuchElementException e) {
      System.out.println("Could not resolve null pointer expression: " + e.getMessage());
      return;
    }

    File patchesDir = new File(projectPath, "patches");
    patchesDir.mkdirs();
    templates.forEach(patch -> {
      File patchDir = new File(patchesDir, patch.getID());
      logger.info("PatchID: {}", patch.getID());
      logger.info("Before: {}", patch.getOriginalStatement());
      patch.apply();
      logger.info("After: {}", patch.getPatchedStatement());
      try {
        patch.store(projectPath, patchDir);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
    });
  }
}