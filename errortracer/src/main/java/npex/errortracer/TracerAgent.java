package npex.errortracer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class TracerAgent {
  public static void premain(String agentArgs, Instrumentation inst) {
    Set<String> packages = new HashSet<>();
    if (agentArgs == null) {
      inst.addTransformer(new TracerTransformer());
      return;
    }

    String[] args = agentArgs.split(",");
    for (File cls : FileUtils.listFiles(new File(args[0]), new String[] { "class" }, true)) {
      String stripped = cls.getAbsolutePath().replaceAll(".*/target/.*classes/", "");
      try {
        String pkg = stripped.substring(0, stripped.lastIndexOf("/")).replace("/", ".");
        packages.add(pkg);
      } catch (StringIndexOutOfBoundsException e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
    System.out.println("Collected packages: " + packages);
    inst.addTransformer(new TracerTransformer(packages, args[1]));
  }
}