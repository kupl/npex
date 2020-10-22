package npex.errortracer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TracerAgent {
  static final Logger logger = LoggerFactory.getLogger(TracerAgent.class);

  public static void premain(String agentArgs, Instrumentation inst) {
    Set<String> packages = new HashSet<>();
    File rootDir = new File(agentArgs != null ? agentArgs : System.getProperty("user.dir"));

    for (File cls : FileUtils.listFiles(rootDir, new String[] { "class" }, true)) {
      String stripped = cls.getAbsolutePath().replaceAll(".*/target/.*classes/", "");
      try {
        String pkg = stripped.substring(0, stripped.lastIndexOf("/")).replace("/", ".");

        packages.add(pkg);
      } catch (StringIndexOutOfBoundsException e) {
      }
    }
    logger.info("Collected packages: {}", packages);
    inst.addTransformer(new TracerTransformer(packages));
  }
}