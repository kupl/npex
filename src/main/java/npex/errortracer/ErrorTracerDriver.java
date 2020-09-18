package npex.errortracer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.MavenLauncher.SOURCE_TYPE;
import spoon.compiler.Environment;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.support.DefaultOutputDestinationHandler;

public class ErrorTracerDriver {
  final private File projectRoot;
  final private MavenLauncher launcher;
  final Environment env;

  public ErrorTracerDriver(final String projectRootPath) {
    this.projectRoot = new File(projectRootPath);
    this.launcher = new MavenLauncher(projectRoot.toString(), SOURCE_TYPE.ALL_SOURCE);
    this.env = this.launcher.getEnvironment();
    this.setLauncher();
  }

  void setLauncher() {
    env.setOutputDestinationHandler(new InplaceOutputHandler(projectRoot, launcher.getEnvironment()));
    env.setAutoImports(false);

    AbstractProcessor<?> methodEntryProcessor = new MethodEntryLoggerProcessor();
    AbstractProcessor<?> invoProcessor = new InvocationLoggerProcessor();
    launcher.addProcessor(methodEntryProcessor);
    launcher.addProcessor(invoProcessor);

  }

  public void run() {
    launcher.run();
  }

  public Launcher getLauncher() {
    return this.launcher;
  }

  private class InplaceOutputHandler extends DefaultOutputDestinationHandler {
    public InplaceOutputHandler(File defaultOutputDirectory, Environment environment) {
      super(defaultOutputDirectory, environment);
    }

    @Override
    public Path getOutputPath(CtModule module, CtPackage pack, CtType type) {
      return Paths.get(type.getPosition().getFile().toString());
    }
  }
}