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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.MethodInfo;
import javassist.expr.ConstructorCall;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

public class TracerTransformer implements ClassFileTransformer {
  static final ClassPool classPool = ClassPool.getDefault();
  final File projectRoot;
  final Set<String> projectPackages;
  final boolean excludeLibraries;
  final String testClassName;
  final String testMethodName;

  public TracerTransformer(File projectRoot, boolean excludeLibraries, Set<String> projectPackages,
      String testMethodArg) {
    this.projectRoot = projectRoot;
    this.projectPackages = projectPackages;
    this.excludeLibraries = excludeLibraries;
    String[] args = testMethodArg.split("#");
    this.testClassName = args[0];
    this.testMethodName = args[1];
  }

  public TracerTransformer(File projectRoot, Set<String> projectPackages, String testMethodArg) {
    this(projectRoot, true, projectPackages, testMethodArg);
  }

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    byte[] byteCode = classfileBuffer;

    CtClass ctClass;
    try {
      ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
    } catch (Exception e) {
      throw new IllegalClassFormatException();
    }

    if (excludeLibraries && !projectPackages.contains(ctClass.getPackageName())) {
      return byteCode;
    }

    /*
     * Instrument call-sites first so that we do not trace instrumented print
     * invocations as logging purpose.
     */
    try {
      String relativeSourcePath = resolveRelativeSourcePath(ctClass);
      for (CtBehavior behavior : ctClass.getDeclaredBehaviors()) {
        behavior.instrument(new InvocationTracer(relativeSourcePath));
      }

      for (CtBehavior behavior : ctClass.getDeclaredBehaviors()) {
        MethodInfo info = behavior.getMethodInfo();
        int lineno = info.getLineNumber(0);
        String entry = getLoggingStmt("ENTRY", relativeSourcePath, behavior.getDeclaringClass().getPackageName(),
            lineno, behavior.getName());
        try {
          behavior.insertBefore(String.format("npex.errortracer.Trace.add(\"%s\");", entry));
        } catch (Exception e) {
          System.out.println("Could not instrument in" + behavior.getName());
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("Could not transform " + ctClass.getName());
    }

    /* Instrument output statement for the trace at the end of the test-method */
    if (ctClass.getName().equals(this.testClassName)) {
      transformTestMethod(ctClass);
    }

    try {
      byteCode = ctClass.toBytecode();
    } catch (IOException | CannotCompileException e) {
      e.printStackTrace();
    }
    return byteCode;
  }

  private void transformTestMethod(CtClass klass) {
    try {
      CtMethod mthd = klass.getDeclaredMethod(testMethodName);
      mthd.insertAfter("npex.errortracer.Trace.print();", true);
    } catch (NotFoundException | CannotCompileException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private String resolveRelativeSourcePath(CtClass klass) {
    String sourceName = klass.getClassFile().getSourceFile();
    String packageAsDirs = klass.getPackageName().replace('.', '/');
    String sourcePathWithPackage = String.format("%s/%s", packageAsDirs, sourceName);
    List<File> found = new ArrayList<>();
    for (File src : FileUtils.listFiles(projectRoot, new String[] { "java" }, true)) {
      if (src.getAbsolutePath().endsWith(sourcePathWithPackage)) {
        found.add(src);
      }
    }

    if (found.size() != 1) {
      System.out.println(String.format("Source file for %s is non-exists or unique!: %s", klass.getName(), found));
    }

    return projectRoot.toURI().relativize(found.get(0).toURI()).getPath();
  }

  private String getLoggingStmt(String tag, String filename, String pkg, int lineno, String element) {
    return String.format("[%s] Filepath: %s, Package: %s, Line: %d, Element: %s", tag, filename, pkg, lineno, element);
  }

  class InvocationTracer extends ExprEditor {
    final String relativeSourcePath;

    public InvocationTracer(String relativeSourcePath) {
      this.relativeSourcePath = relativeSourcePath;
    }

    private void _edit(Expr expr, String element) {
      int lineno = expr.getLineNumber();
      try {
        String entry = getLoggingStmt("CALLSITE", relativeSourcePath, expr.getEnclosingClass().getPackageName(), lineno,
            element);
        String toBeInserted = String.format("{ npex.errortracer.Trace.add(\"%s\"); $_ = $proceed($$);}", entry);
        expr.replace(toBeInserted);
      } catch (CannotCompileException e) {
      }
    }

    @Override
    public void edit(MethodCall invo) {
      _edit(invo, invo.getMethodName());
    }

    @Override
    public void edit(ConstructorCall cc) {
      _edit(cc, "<init>");
    }

    @Override
    public void edit(NewExpr newExpr) {
      _edit(newExpr, newExpr.getClassName());
    }
  }
}