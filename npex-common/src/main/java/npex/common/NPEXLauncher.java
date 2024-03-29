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
package npex.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.MavenLauncher.SOURCE_TYPE;
import spoon.compiler.Environment;
import spoon.reflect.factory.Factory;
import spoon.support.SerializationModelStreamer;

public abstract class NPEXLauncher {
  final private static String SPOON_MODEL_CACHE_NAME = ".spoon-model.cache";
  final protected static Logger logger = LoggerFactory.getLogger(NPEXLauncher.class);

  final protected File projectRoot;
  final protected String projectName;
  final private File spoonModelCacheFile;

  final protected Factory factory;
  final protected Launcher spoonLauncher;

  private Launcher buildSpoonLauncher(File projectRoot, String[] classpath, SOURCE_TYPE sourceType) throws IOException {
    Launcher launcher;
    if (new File(projectRoot, "pom.xml").exists()) {
      launcher = createMavenLauncher(projectRoot, sourceType);
    } else if (new File(projectRoot, "ant").exists() || (new File(projectRoot, "build.xml").exists())) {
      launcher = createAntLauncher(projectRoot);
    } else {
      launcher = createJavacLauncher(projectRoot);
    }
    Environment env = launcher.getEnvironment();
    if (classpath != null)
      env.setSourceClasspath(classpath);
    else
      env.setNoClasspath(true);
    env.setIgnoreDuplicateDeclarations(true);
    env.setShouldCompile(false);
    env.disableConsistencyChecks();
    launcher.buildModel();
    new SerializationModelStreamer().save(launcher.getFactory(), new FileOutputStream(spoonModelCacheFile));
    return launcher;
  }

  private Launcher spoonLauncherFromCache() throws IOException {
    Factory factory = new SerializationModelStreamer().load(new FileInputStream(spoonModelCacheFile));
    logger.info("Cached spoon model has been found! (created on {})",
        Files.readAttributes(spoonModelCacheFile.toPath(), BasicFileAttributes.class).creationTime());
    return new Launcher(factory);
  }

  public NPEXLauncher(File projectRoot, boolean loadFromCache, String[] classpath, SOURCE_TYPE sourceType) throws IOException {
    this.projectRoot = projectRoot;
    this.projectName = projectRoot.getName();
    this.spoonModelCacheFile = new File(projectRoot, SPOON_MODEL_CACHE_NAME);
    this.spoonLauncher = loadFromCache ? spoonLauncherFromCache() : buildSpoonLauncher(projectRoot, classpath, sourceType);
    var env = spoonLauncher.getEnvironment();
    env.setIgnoreDuplicateDeclarations(true);
    env.setShouldCompile(false);
    env.disableConsistencyChecks();
    System.out.println(spoonLauncher.getEnvironment().getNoClasspath());
    this.factory = spoonLauncher.getFactory();
  }

  public NPEXLauncher(File projectRoot, boolean loadFromCache, String[] classpath) throws IOException {
    this(projectRoot, loadFromCache, classpath, SOURCE_TYPE.ALL_SOURCE);
  }

  public NPEXLauncher(File projectRoot) throws IOException {
    this(projectRoot, false, new String[] {}, SOURCE_TYPE.ALL_SOURCE);
  }

  static private MavenLauncher createMavenLauncher(File projectRoot, SOURCE_TYPE sourceType) {
    logger.info("Parsing maven project ...");
    return new MavenLauncher(projectRoot.getAbsolutePath(), sourceType);
  }

  static private Launcher createAntLauncher(File projectRoot) {
    logger.info("Parsing ant project ...");
    Launcher launcher = new Launcher();
    IOFileFilter dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.or(FileFilterUtils.nameFileFilter("target"),
        FileFilterUtils.nameFileFilter("spooned"), FileFilterUtils.nameFileFilter("patches")));
    FileUtils.listFiles(projectRoot, new SuffixFileFilter(".java"), dirFilter)
        .forEach(src -> launcher.addInputResource(src.getAbsolutePath()));
    return launcher;
  }

  static private Launcher createJavacLauncher(File projectRoot) {
    logger.info("Parsing project without supported build systems ...");
    Launcher launcher = new Launcher();
    IOFileFilter dirFilter = FileFilterUtils.notFileFilter(FileFilterUtils.or(FileFilterUtils.nameFileFilter("target"),
        FileFilterUtils.nameFileFilter("spooned"), FileFilterUtils.nameFileFilter("patches")));
    FileUtils.listFiles(projectRoot, new SuffixFileFilter(".java"), dirFilter)
        .forEach(src -> launcher.addInputResource(src.getAbsolutePath()));
    return launcher;
  }

  public abstract void run() throws NPEXException;
}