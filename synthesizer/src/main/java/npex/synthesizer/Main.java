package npex.synthesizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import npex.synthesizer.errortracerjp.ErrorTracerDriverJP;
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

public class Main {
  static PatchStrategy[] strategies = new PatchStrategy[] { new SkipBreakStrategy(), new SkipContinueStrategy(),
      new SkipSinkStatementStrategy(), new SkipReturnStrategy(), new SkipBlockStrategy(),
      new InitPointerStrategy(new VarInitializer()), new InitPointerStrategy(new ObjectInitializer()),
      new ReplacePointerStrategy(new VarInitializer()), new ReplacePointerStrategy(new ObjectInitializer()),
      new ReplacePointerStrategy(new PrimitiveInitializer()), new ReplaceEntireExpressionStrategy(new VarInitializer()),
      new ReplaceEntireExpressionStrategy(new ObjectInitializer()),
      new ReplaceEntireExpressionStrategy(new PrimitiveInitializer()) };

  public static void main(String[] args) throws FileNotFoundException {
    Options options = new Options();
    Option opt_help = new Option("help", false, "print help message");
    Option opt_patch = new Option("patch", true, "Generate patches for given NPE");
    Option opt_extract = new Option("extract", true, "Extract buggy codes from existing null handles");
    Option opt_trace = new Option("trace", true, "Instrument call tracer");
    opt_patch.setArgs(2);
    opt_extract.setArgs(2);
    opt_trace.setArgs(2);

    options.addOption(opt_help);
    options.addOption(opt_patch);
    options.addOption(opt_extract);
    options.addOption(opt_trace);
    CommandLineParser parser = new DefaultParser();
    CommandLine line;

    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      e.printStackTrace();
      throw new IllegalArgumentException();
    }

    if (line.hasOption("help")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("npex-synthesizer", options);
      return;
    }

    if (line.hasOption("patch")) {
      String[] values = line.getOptionValues("patch");

      PatchSynthesizer synthesizer = new PatchSynthesizer(values[0], new ArrayList<>(Arrays.asList(strategies)));
      List<PatchTemplate> templates = new ArrayList<>();
      try {
        CtExpression<?> nullExp = NPEInfo.readFromJSON(synthesizer.getFactory(), values[1]).resolve();
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

      File patchesDir = new File(values[0], "patches");
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
          patch.store(values[0], patchDir);
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }
      });

      return;
    }

    if (line.hasOption("extract")) {
      String[] values = line.getOptionValues("extract");
      Driver driver = new Driver(values[0], values[1]);
      driver.run();

      return;
    }

    if (line.hasOption("trace")) {
      String[] values = line.getOptionValues("trace");
      ErrorTracerDriverJP driver = new ErrorTracerDriverJP(values[0], values[1]);
      driver.run();
    }
  }
}