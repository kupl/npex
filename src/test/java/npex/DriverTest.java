package npex;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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

public class DriverTest {
  @Test
  public void test() throws Exception {
    final String projectRootPath = "/media/4tb/npex/npex_data/benchmarks/sling-org-apache-sling-tracer/";
    final String learnigDataDir = "/media/4tb/npex/learningData";
    Driver driver = new Driver(projectRootPath, learnigDataDir);
    driver.run();
  }

  @Test
  public void testNullExp() throws Exception {
    MavenPatchExtractor mvn = new MavenPatchExtractor("/home/junhee/npex/benchmarks-bears/Bears-169-buggy");
    CtExpression<?> nullExp = (CtExpression<?>) Utils.resolveNullPointer(mvn.getFactory(),
        "/home/junhee/npex/benchmarks-bears/Bears-169-buggy/npe.json");

    List<PatchStrategy> strategies = new ArrayList<>();
    strategies.add(new SkipBreakStrategy());
    strategies.add(new SkipContinueStrategy());
    strategies.add(new SkipSinkStatementStrategy());
    strategies.add(new SkipReturnStrategy());
    strategies.add(new SkipBlockStrategy());
    strategies.add(new InitPointerStrategy(new VarInitializer()));
    strategies.add(new InitPointerStrategy(new ObjectInitializer()));
    strategies.add(new ReplacePointerStrategy(new VarInitializer()));
    strategies.add(new ReplacePointerStrategy(new ObjectInitializer()));
    strategies.add(new ReplaceEntireExpressionStrategy(new VarInitializer()));
    strategies.add(new ReplaceEntireExpressionStrategy(new ObjectInitializer()));

    List<PatchTemplate> templates = new ArrayList<PatchTemplate>();
    strategies.forEach(stgy -> {
      if (stgy.isApplicable(nullExp)) {
        System.out.println(String.format("Strategy %s is applicable!", stgy.getName()));
        templates.add(stgy.generate(nullExp));
      } else
        System.out.println(String.format("Strategy %s is not applicable!", stgy.getName()));
    });

    templates.forEach(template -> {
      System.out.println("Before: " + template.getBlock());
      template.apply();
      System.out.println("After: " + template.getBlock());
    });
  }

}