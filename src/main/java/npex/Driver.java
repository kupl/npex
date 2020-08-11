package npex;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import npex.buggycode.BuggyCode;
import npex.buggycode.NullHandle;
import npex.strategy.InitPointerStrategy;
import npex.strategy.ObjectInitializer;
import npex.strategy.PatchStrategy;
import npex.strategy.ReplacePointerStrategy;
import npex.strategy.ReplaceSinkExprStrategy;
import npex.strategy.SkipBreakStrategy;
import npex.strategy.SkipContinueStrategy;
import npex.strategy.SkipReturnStrategy;
import npex.strategy.SkipSinkStatementStrategy;
import npex.strategy.VarInitializer;
import npex.template.PatchTemplate;
import npex.template.SourceChange;
import spoon.reflect.code.CtExpression;

public class Driver {
  final private File projectRoot;
  final private String projectName;
  final private File projectDataDirectory;
  final private File projectBugsDirectory;
  final private MavenPatchExtractor extractor;
  final private List<PatchStrategy> strategies = new ArrayList<PatchStrategy>();
  protected Logger logger = Logger.getLogger(Driver.class);

  public Driver(String projectRootPath, String learningDataDirectory) throws Exception {
    this.projectRoot = new File(projectRootPath);
    this.projectName = projectRoot.getName();
    this.projectDataDirectory = new File(learningDataDirectory + "/" + this.projectName);
    this.projectDataDirectory.mkdirs();
    this.projectBugsDirectory = new File(this.projectDataDirectory, "bugs");
    this.extractor = new MavenPatchExtractor(projectRootPath);

    this.strategies.add(new SkipBreakStrategy());
    this.strategies.add(new SkipContinueStrategy());
    this.strategies.add(new SkipSinkStatementStrategy());
    this.strategies.add(new SkipReturnStrategy());
    this.strategies.add(new InitPointerStrategy(new VarInitializer()));
    this.strategies.add(new InitPointerStrategy(new ObjectInitializer()));
    this.strategies.add(new ReplacePointerStrategy(new VarInitializer()));
    this.strategies.add(new ReplacePointerStrategy(new ObjectInitializer()));
    this.strategies.add(new ReplaceSinkExprStrategy(new VarInitializer()));
    this.strategies.add(new ReplaceSinkExprStrategy(new ObjectInitializer()));
  }

  private List<BuggyCode> generateBuggyCodesApplied() {
    List<NullHandle> nullHandles = extractor.extractNullHandles();
    List<BuggyCode> buggyCodes = nullHandles.stream().map(x -> new BuggyCode(this.projectName, x))
        .filter(x -> x.hasNullPointerIdentifiable() && x.isAccessPathResolved() && !x.isBugInConstructor())
        .collect(Collectors.toList());
    buggyCodes.forEach(x -> {
      try {
        logger.info("Generating buggy code " + x.getID());
        x.stripNullHandle();
      } catch (Exception e) {
        logger.error("-- Exception occurs in stripNullHandle");
        logger.error("-- " + e.toString());
      }
    });

    return buggyCodes;
  }

  private List<PatchTemplate> generatePatches(BuggyCode buggy) {
    logger.info("Generating patches for buggy code: " + buggy.getID());
    List<PatchTemplate> templates = new ArrayList<PatchTemplate>();
    CtExpression<?> nullPointer = buggy.getNullPointer();
    templates.add(buggy.generateDeveloperPatch());

    this.strategies.forEach(stgy -> {
      if (stgy.isApplicable(nullPointer)) {
        logger.info(String.format("Strategy %s is applicable!", stgy.getName()));
        templates.add(stgy.generate(nullPointer));
      } else {
        logger.info(String.format("Strategy %s is not applicable!", stgy.getName()));
      }
      ;
    });

    return templates;
  }

  private void doBuggyCode(BuggyCode buggy) throws IOException {
    /* Create directory for the buggy code */
    File bugDirectoryFile = new File(this.projectBugsDirectory, buggy.getID());
    bugDirectoryFile.mkdirs();
    File buggySourceFile = new File(bugDirectoryFile, "buggy.java");
    buggy.getSourceChange().writeChangeToSourceCode(buggySourceFile);

    File patchesDir = new File(bugDirectoryFile, "patches");
    patchesDir.mkdirs();

    generatePatches(buggy).forEach(patch -> {
      File patchDir = new File(patchesDir, patch.getID());
      patchDir.mkdirs();
      try {
        File patchSourceFile = new File(patchDir, "patch.java");
        File jsonOutFile = new File(patchDir, "patch.json");
        SourceChange<?> change = patch.getSourceChange();
        change.writeChangeToSourceCode(patchSourceFile);
        change.writeChangeToJson(this.projectRoot.toString(), jsonOutFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

  }


  public void run() {
    logger.info("@@ Generating buggy codes");
    List<BuggyCode> buggyCodes = generateBuggyCodesApplied();
    logger.info("@@ Generating buggy codes comple");
    buggyCodes.forEach(buggy -> {
      try {
        doBuggyCode(buggy);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }
}