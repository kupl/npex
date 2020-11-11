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

import org.apache.log4j.Logger;

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

public class Driver {
  final private File projectRoot;
  final private File projectDataDirectory;
  final private File projectBugsDirectory;
  final private String projectName;
  final private PatchSynthesizer extractor;
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
    this.extractor = new PatchSynthesizer(projectRootPath, new ArrayList<>(Arrays.asList(strategies)));
  }

  private void doBuggyCode(final BuggyCode buggy) {
    try {
      final File bugDirectoryFile = new File(this.projectBugsDirectory, buggy.getID());
      bugDirectoryFile.mkdirs();
      final File buggySourceFile = new File(bugDirectoryFile, "buggy.java");
      File outFile = new File(bugDirectoryFile, "npe.json");
      buggy.getNPEInfo().writeToJSON(outFile);
      buggy.getSourceChange().writeChangeToSourceCode(buggySourceFile);
    } catch (IOException e) {
      logger.fatal(String.format("%s: Failed to generate npe.json and buggy source", buggy.getID()));
    }
  }

  public void run() {
    final List<BuggyCode> buggyCodes = extractor.extractBuggyCodes();
    buggyCodes.forEach(buggy -> {
      doBuggyCode(buggy);
    });
  }
}