package npex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

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

public class Driver {
  final private File projectRoot;
  final private File projectDataDirectory;
  final private File projectBugsDirectory;
  final private String projectName;
  final private MavenPatchExtractor extractor;
  final private PatchStrategy[] strategies;
  protected Logger logger = Logger.getLogger(Driver.class);

  public Driver(final String projectRootPath, final String learningDataDirectory) {
    this.projectRoot = new File(projectRootPath);
    this.projectName = projectRoot.getName();
    this.projectDataDirectory = new File(learningDataDirectory + "/" + this.projectName);
    this.projectBugsDirectory = new File(this.projectDataDirectory, "bugs");
    this.projectBugsDirectory.mkdirs();

    this.strategies = new PatchStrategy[] { new SkipBreakStrategy(), new SkipContinueStrategy(),
        new SkipSinkStatementStrategy(), new SkipReturnStrategy(), new SkipBlockStrategy(),
        new InitPointerStrategy(new VarInitializer()), new InitPointerStrategy(new ObjectInitializer()),
        new ReplacePointerStrategy(new VarInitializer()), new ReplacePointerStrategy(new ObjectInitializer()),
        new ReplaceEntireExpressionStrategy(new VarInitializer()),
        new ReplaceEntireExpressionStrategy(new ObjectInitializer()) };
    this.extractor = new MavenPatchExtractor(projectRootPath, new ArrayList<>(Arrays.asList(strategies)));
  }

  private void doBuggyCode(final BuggyCode buggy) {
    /* Prepare directory for the buggy code */
    try {
      final File bugDirectoryFile = new File(this.projectBugsDirectory, buggy.getID());
      bugDirectoryFile.mkdirs();
      final File buggySourceFile = new File(bugDirectoryFile, "buggy.java");
      buggy.getSourceChange().writeChangeToSourceCode(buggySourceFile);

      final File patchesDir = new File(bugDirectoryFile, "patches");
      patchesDir.mkdirs();

      extractor.generatePatchTemplates(buggy).forEach(patch -> {
        try {
          final File patchDir = new File(patchesDir, patch.getID());
          patch.store(projectRoot.getAbsolutePath(), patchDir);
          File outFile = new File(bugDirectoryFile, "npe.json");
          NPEInfo npe = buggy.getNPEInfo();
          npe.writeToJSON(outFile);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    } catch (Exception e) {
    }
  }

  public void run() {
    final List<BuggyCode> buggyCodes = extractor.extractBuggyCodes();
    buggyCodes.forEach(buggy -> {
      doBuggyCode(buggy);
    });
  }
}