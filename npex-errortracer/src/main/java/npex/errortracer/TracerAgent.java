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
package npex.errortracer;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class TracerAgent {
  public static void premain(String agentArgs, Instrumentation inst) {
    Set<String> packages = new HashSet<>();

    String[] args = agentArgs.split(",");

    File projectRoot = new File(args[0]);

    for (File cls : FileUtils.listFiles(projectRoot, new String[] { "class" }, true)) {
      String stripped = cls.getAbsolutePath().replaceAll(".*/target/.*classes/", "");
      try {
        String pkg = stripped.substring(0, stripped.lastIndexOf("/")).replace("/", ".");
        packages.add(pkg);
      } catch (StringIndexOutOfBoundsException e) {
        packages.add(stripped);
      }
    }
    System.out.println("Collected packages: " + packages);
    inst.addTransformer(new TracerTransformer(projectRoot, packages, args[1]));
  }
}