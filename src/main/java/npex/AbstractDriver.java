package npex;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.MavenLauncher.SOURCE_TYPE;

public abstract class AbstractDriver {
  final protected File projectRoot;
  final protected Logger logger;
  final protected Launcher launcher;

  public AbstractDriver(final String projectRootPath) {
    this.projectRoot = new File(projectRootPath);
    this.logger = Logger.getLogger(this.getClass());

    if (new File(projectRoot, "pom.xml").exists()) {
      this.launcher = new MavenLauncher(projectRootPath, SOURCE_TYPE.ALL_SOURCE);
    } else {
      this.launcher = new Launcher();
      FileUtils.listFiles(projectRoot, new String[] { "java" }, false)
          .forEach(f -> launcher.addInputResource(f.toString()));
    }

    this.setupLauncher();
  }

  protected abstract void setupLauncher();
}