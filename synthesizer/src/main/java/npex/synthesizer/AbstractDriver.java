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