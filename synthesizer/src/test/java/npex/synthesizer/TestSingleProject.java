package npex.synthesizer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.junit.Test;

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

  protected Logger logger = Logger.getLogger(TestSingleProject.class);

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
          List<PatchTemplate> generated = stgy.generate(nullExp);
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
      System.out.println("ID: " + patch.getID());
      System.out.println("Before: " + patch.getBlock());
      patch.apply();
      System.out.println("PatchBlock type: " + patch.getBlock().getClass());
      System.out.println(patch.getBlock());
      System.out.println("After: " + patch.getBlock());
      try {
        patch.store(projectPath, patchDir);
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
    });
  }
}