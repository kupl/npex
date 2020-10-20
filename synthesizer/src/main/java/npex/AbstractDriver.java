package npex.synthesizer;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.log4j.Logger;

import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.MavenLauncher.SOURCE_TYPE;

public abstract class AbstractDriver {
  final protected File projectRoot;
  final protected String projectName;
  final protected Logger logger;
  final protected Launcher launcher;

  public AbstractDriver(final String projectRootPath) {
    this.projectRoot = new File(projectRootPath);
    this.projectName = projectRoot.getName();
    this.logger = Logger.getLogger(this.getClass());

    if (new File(projectRoot, "pom.xml").exists()) {
      logger.debug("pom.xml is found: Parsing maven project ...");
      this.launcher = new MavenLauncher(projectRootPath, SOURCE_TYPE.ALL_SOURCE);
    } else if (new File(projectRoot, "ant").exists() || (new File(projectRoot, "build.xml").exists())) {
      logger.debug("build.xml is found: Parsing ant project ...");
      this.launcher = new Launcher();
      IOFileFilter dirFilter = FileFilterUtils
          .notFileFilter(FileFilterUtils.or(FileFilterUtils.nameFileFilter("target"),
              FileFilterUtils.nameFileFilter("spooned"), FileFilterUtils.nameFileFilter("patches")));
      FileUtils.listFiles(projectRoot, new SuffixFileFilter(".java"), dirFilter)
          .forEach(src -> launcher.addInputResource(src.getAbsolutePath()));
    } else {
      this.launcher = new Launcher();
      Collection<File> sources = FileUtils.listFiles(projectRoot, new SuffixFileFilter(".java"), null);
      sources.forEach(src -> launcher.addInputResource(src.getAbsolutePath()));
    }

    this.setupLauncher();
    this.launcher.run();
  }

  protected abstract void setupLauncher();
}