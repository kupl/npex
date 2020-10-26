package npex.errortracer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

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
  final Set<String> projectPackages;
  final boolean excludeLibraries;
  final String testClassName;
  final String testMethodName;

  public TracerTransformer() {
    this.projectPackages = null;
    this.excludeLibraries = false;
    this.testClassName = "";
    this.testMethodName = "";
  }

  public TracerTransformer(Set<String> projectPackages, String testMethodArg) {
    this.projectPackages = projectPackages;
    this.excludeLibraries = true;
    String[] args = testMethodArg.split("#");
    this.testClassName = args[0];
    this.testMethodName = args[1];
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
      for (CtBehavior behavior : ctClass.getDeclaredBehaviors()) {
        behavior.instrument(new InvocationTracer());
      }

      for (CtBehavior behavior : ctClass.getDeclaredBehaviors()) {
        MethodInfo info = behavior.getMethodInfo();
        int lineno = info.getLineNumber(0);
        String entry = getLoggingStmt("ENTRY", behavior.getDeclaringClass().getClassFile().getSourceFile(),
            behavior.getDeclaringClass().getPackageName(), lineno, behavior.getName());
        try {
          behavior.insertBefore(String.format("npex.errortracer.Trace.add(\"%s\");", entry));
        } catch (Exception e) {
          System.out.println("Could not instrument in" + behavior.getName());
        }
      }
    } catch (Exception ex) {
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

  private String getLoggingStmt(String tag, String filename, String pkg, int lineno, String element) {
    return String.format("[%s] Filepath: %s, Package: %s, Line: %d, Element: %s", tag, filename, pkg, lineno, element);
  }

  class InvocationTracer extends ExprEditor {
    private void _edit(Expr expr, String element) {
      String filename = expr.getFileName();
      int lineno = expr.getLineNumber();
      try {
        String entry = getLoggingStmt("CALLSITE", filename, expr.getEnclosingClass().getPackageName(), lineno, element);
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