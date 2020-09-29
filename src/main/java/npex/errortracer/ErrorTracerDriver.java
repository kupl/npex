package npex.errortracer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import npex.AbstractDriver;
import spoon.compiler.Environment;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.support.DefaultOutputDestinationHandler;

public class ErrorTracerDriver extends AbstractDriver {
  public ErrorTracerDriver(final String projectRootPath) {
    super(projectRootPath);
  }

  protected void setupLauncher() {
    Environment env = this.launcher.getEnvironment();
    env.setOutputDestinationHandler(new InplaceOutputHandler(projectRoot, launcher.getEnvironment()));
    env.setAutoImports(false);

    AbstractProcessor<?> methodEntryProcessor = new MethodEntryLoggerProcessor(projectRoot);
    AbstractProcessor<?> invoProcessor = new InvocationLoggerProcessor(projectRoot);
    launcher.addProcessor(methodEntryProcessor);
    launcher.addProcessor(invoProcessor);
  }

  public void run() {
    launcher.run();
  }

  private class InplaceOutputHandler extends DefaultOutputDestinationHandler {
    public InplaceOutputHandler(File defaultOutputDirectory, Environment environment) {
      super(defaultOutputDirectory, environment);
    }

    @Override
    public Path getOutputPath(CtModule module, CtPackage pack, CtType type) {
      try {
        return Paths.get(type.getPosition().getFile().getParent(), getFileName(pack, type));
      } catch (NullPointerException e) {
        logger.fatal(e.getMessage());
        return Paths.get("./wrong_address.java");
      }
    }
  }
}