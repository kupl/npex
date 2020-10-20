package npex.errortracer;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.bytecode.MethodInfo;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

public class TracerTransformer implements ClassFileTransformer {
  final ClassPool classPool = ClassPool.getDefault();

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    byte[] byteCode = classfileBuffer;

    try {
      CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
      if (ctClass.getPackageName() != null)
        return byteCode;

      /*
       * Instrument call-site first so that we do not trace instrumented print
       * invocations as logging purpose.
       */
      for (CtBehavior behavior : ctClass.getDeclaredBehaviors()) {
        behavior.instrument(new InvocationTracer());
      }

      for (CtBehavior behavior : ctClass.getDeclaredBehaviors()) {
        behavior.insertBefore(getLoggingStmt(behavior));
      }

      byteCode = ctClass.toBytecode();
      ctClass.detach();
    } catch (Throwable ex) {
      System.out.println("Exception: " + ex);
      ex.printStackTrace();
    }
    return byteCode;
  }

  private String getLoggingStmt(String tag, String filename, int lineno, String element) {
    return String.format("System.out.println(\"[%s] Filepath: %s, Line: %d, Element: %s\");", tag, filename, lineno,
        element);
  }

  private String getLoggingStmt(CtBehavior behavior) {
    MethodInfo info = behavior.getMethodInfo();
    return getLoggingStmt("ENTRY", behavior.getDeclaringClass().getClassFile().getSourceFile(), info.getLineNumber(0),
        behavior.getName());
  }

  class InvocationTracer extends ExprEditor {
    @Override
    public void edit(MethodCall invo) throws CannotCompileException {
      String filename = invo.getFileName();
      int lineno = invo.getLineNumber();
      String element = invo.getMethodName();
      invo.replace(String.format("%s $_ = $proceed($$);", getLoggingStmt("CALLSITE", filename, lineno, element)));
    }

    @Override
    public void edit(ConstructorCall cc) throws CannotCompileException {
      String filename = cc.getFileName();
      int lineno = cc.getLineNumber();
      String element = "<init>";
      cc.replace(String.format("%s $_ = $proceed($$);", getLoggingStmt("CALLSITE", filename, lineno, element)));
    }

    @Override
    public void edit(NewExpr newExpr) throws CannotCompileException {
      String filename = newExpr.getFileName();
      int lineno = newExpr.getLineNumber();
      String element = newExpr.getClassName();
      newExpr.replace(String.format("%s $_ = $proceed($$);", getLoggingStmt("CALLSITE", filename, lineno, element)));
    }
  }
}
