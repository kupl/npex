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
package npex.driver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import npex.common.DefaultNPEXLauncher;
import npex.common.NPEXLauncher;
import npex.extractor.invocation.InvocationContextExtractorLauncher;
import npex.extractor.nullhandle.NullHandleExtractorLauncher;
import npex.extractor.runtime.RuntimeMethodInfoExtractorLauncher;
import npex.synthesizer.SynthesizerLauncher;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import spoon.MavenLauncher.SOURCE_TYPE;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

@Command(name = "npex", subcommands = { BuildCommand.class, PatchCommand.class, HandleExtractorCommand.class,
    InvocationExtractor.class, RuntimeMethodInfoExtractor.class, CommandLine.HelpCommand.class }, mixinStandardHelpOptions = true)

public class Main {
  final static Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
  static {
    rootLogger.setLevel(Level.WARN);
  }

  @Option(names = { "-g", "-debug" }, description = "Set logging level to debug (default level: WARN)")
  private void setDebugLoggingLevel(boolean isDebugMode) {
    rootLogger.setLevel(Level.DEBUG);
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
  }
}

abstract class SpoonCommand implements Runnable {
  @Spec
  protected CommandSpec spec;

  @Parameters(paramLabel = "<PROJECT_ROOT>", description = "path for project root directory")
  protected File projectRoot;

  @Option(names = { "-c", "--cached" }, paramLabel = "<LOAD_CACHED_MODEL>", description = "load cached spoon model")
  protected boolean loadSpoonModelFromCache;

  @Option(names = { "-cp",
      "--classpath" }, split = ":", paramLabel = "<CLASSPATH>", description = "set source class path")
  protected String[] classpath;

  private void checkFileParametersValidity() {
    List<ArgSpec> argSpecs = new ArrayList<>();
    argSpecs.addAll(spec.options());
    argSpecs.addAll(spec.positionalParameters());
    for (ArgSpec option : argSpecs)
      if (option.getValue() instanceof File f) {
        if (!f.exists())
          throw new ParameterException(spec.commandLine(), "File does not exists at " + f.getAbsolutePath());
      }
  }

  protected void resetUnspecifiedOptionValue(String name, Object value) {
    if (!spec.commandLine().getParseResult().hasMatchedOption(name)) {
      spec.findOption(name).setValue(value);
    }
  }

  public void run() {
    checkFileParametersValidity();
    try {
      launch(spec.commandLine().getParseResult());
    } catch (IOException e) {
    }
  }

  protected abstract void launch(ParseResult pr) throws IOException;

}

@Command(name = "build")
class BuildCommand extends SpoonCommand {

  public void launch(ParseResult pr) throws IOException {
    new DefaultNPEXLauncher(projectRoot, false, classpath);
  }

}

@Command(name = "patch")
class PatchCommand extends SpoonCommand {
  static final String defaultNPEReportName = "npe.json";

  @Option(names = { "-r",
      "--report" }, paramLabel = "<NPE_REPORT>", defaultValue = defaultNPEReportName, description = "path for JSON-formatted NPE report (default: <PROJECT_ROOT>/${DEFAULT-VALUE})")
  File npeReport;

  public void launch(ParseResult pr) throws IOException {
    if (!pr.hasMatchedOption("report")) {
      spec.findOption("--report").setValue(new File(projectRoot, defaultNPEReportName));
    }
    NPEXLauncher launcher = new SynthesizerLauncher(projectRoot, loadSpoonModelFromCache, classpath, npeReport);
    launcher.run();
  }
}

@Command(name = "handle-extractor")
class HandleExtractorCommand extends SpoonCommand {
  static final String defaultResultsName = "handles.npex.json";

  @Option(names = { "-r",
      "--results" }, paramLabel = "<RESULTS_JSON>", defaultValue = defaultResultsName, description = "path for results JSON file where collected handles information to be stored (default:<PROJECT_ROOT>/${DEFAULT-VALUE})")
  String resultsPath;

  public void launch(ParseResult pr) throws IOException {
    if (!pr.hasMatchedOption("--results")) {
      spec.findOption("--results").setValue(new File(projectRoot, resultsPath).getAbsolutePath());
    }
    NPEXLauncher launcher = new NullHandleExtractorLauncher(projectRoot, loadSpoonModelFromCache, classpath,
        resultsPath);
    launcher.run();
  }
}

@Command(name = "extract-invo-context")
class InvocationExtractor extends SpoonCommand {
  static final String defaultResultsName = "invo-ctx.npex.json";

  @Option(names = { "-r",
      "--results" }, paramLabel = "<RESULTS_JSON>", defaultValue = defaultResultsName, description = "path for results JSON file where collected handles information to be stored (default:<PROJECT_ROOT>/${DEFAULT-VALUE})")
  String resultsPath;

  @Option(names = { "-t", "--trace" }, paramLabel = "<TRACE_JSON>", description = "path for trace JSON")
  File trace;

  public void launch(ParseResult pr) throws IOException {
    if (!pr.hasMatchedOption("--results")) {
      spec.findOption("--results").setValue(new File(projectRoot, resultsPath).getAbsolutePath());
    }

    NPEXLauncher launcher = new InvocationContextExtractorLauncher(projectRoot, loadSpoonModelFromCache, classpath,
        resultsPath, trace);
    launcher.run();
  }
}

@Command(name = "rt-method")
class RuntimeMethodInfoExtractor extends SpoonCommand {
  static final String defaultResultsName = "";
  @Option(names = { "-r",
      "--results" }, paramLabel = "<RESULTS_JSON>", defaultValue = defaultResultsName, description = "path for results JSON file where collected handles information to be stored (default:<PROJECT_ROOT>/${DEFAULT-VALUE})")
  String resultsPath;

  public void launch(ParseResult pr) throws IOException {
    if (!pr.hasMatchedOption("--results")) {
      spec.findOption("--results").setValue(new File(projectRoot, resultsPath).getAbsolutePath());
    }

    NPEXLauncher launcher = new RuntimeMethodInfoExtractorLauncher(projectRoot, loadSpoonModelFromCache, classpath,
        resultsPath);
    launcher.run();
  }
}
