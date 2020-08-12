package npex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

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
import npex.template.SourceChange;
import spoon.reflect.code.CtExpression;

public class Main {
  static Logger logger = Logger.getLogger(Main.class);

  public static void main(String[] args) {
    Options options = new Options();
    Option opt_patch = new Option("patch", true, "Generate patches for given NPE");
    opt_patch.setArgs(2);

    options.addOption(opt_patch);
    CommandLineParser parser = new DefaultParser();
    CommandLine line;

    try {
      line = parser.parse(options, args);
      String[] values = line.getOptionValues("patch");
      MavenPatchExtractor mvn = new MavenPatchExtractor(values[0]);
      CtExpression<?> nullExp = (CtExpression<?>) Utils.resolveNullPointer(mvn.getFactory(), values[1]);

      List<PatchStrategy> strategies = new ArrayList<PatchStrategy>();
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

      File projectRoot = new File(values[0]);
      File patchesDir = new File(projectRoot, "patches");
      patchesDir.mkdirs();

      templates.forEach(patch -> {
        File patchDir = new File(patchesDir, patch.getID());
        patchDir.mkdirs();
        System.out.println("ID: " + patch.getID());
        System.out.println("Before: " + patch.getBlock());
        patch.apply();
        System.out.println("After: " + patch.getBlock());
        try {
          SourceChange<?> change = patch.getSourceChange();
          change.writeChangeToSourceCode(new File(patchDir, "patch.java"));
          change.writeChangeToJson(projectRoot.getAbsolutePath(), new File(patchDir, "patch.json"));
        } catch (NullPointerException e) {
          e.printStackTrace();
          logger.fatal(e.toString());
          logger.fatal("Could not generate source change (Constructor?)");
        } catch (IOException e) {
          logger.fatal(e.toString());
          logger.fatal("IO exception occurs in writing source and json");
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}