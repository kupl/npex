package npex.errortracer;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.bytecode.MethodInfo;
import javassist.expr.ConstructorCall;
import javassist.expr.Expr;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

public class TracerTransformer implements ClassFileTransformer {
  final ClassPool classPool = ClassPool.getDefault();
  final Set<String> projectPackages;
  final boolean excludeLibraries;

  final static Logger logger = LoggerFactory.getLogger(TracerTransformer.class);

  public TracerTransformer() {
    this.projectPackages = null;
    this.excludeLibraries = false;
  }

  public TracerTransformer(Set<String> projectPackages) {
    this.projectPackages = projectPackages;
    this.excludeLibraries = true;
  }

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    byte[] byteCode = classfileBuffer;

    try {
      CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
      if (excludeLibraries && !projectPackages.contains(ctClass.getPackageName())) {
        return byteCode;
      }

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
      // logger.error("Exception occurs while transforming {}", className, ex);
    }
    return byteCode;
  }

  private String getLoggingStmt(String tag, String filename, String pkg, int lineno, String element) {
    return String.format("System.out.println(\"[%s] Filepath: %s, Package: %s, Line: %d, Element: %s\");", tag,
        filename, pkg, lineno, element);
  }

  private String getLoggingStmt(CtBehavior behavior) {
    MethodInfo info = behavior.getMethodInfo();
    return getLoggingStmt("ENTRY", behavior.getDeclaringClass().getClassFile().getSourceFile(),
        behavior.getDeclaringClass().getPackageName(), info.getLineNumber(0), behavior.getName());
  }

  class InvocationTracer extends ExprEditor {
    private void _edit(Expr expr, String element) {
      String filename = expr.getFileName();
      int lineno = expr.getLineNumber();
      try {
        expr.replace(String.format("%s $_ = $proceed($$);",
            getLoggingStmt("CALLSITE", filename, expr.getEnclosingClass().getPackageName(), lineno, element)));
      } catch (CannotCompileException e) {
        logger.error("Could not instrument invocation site of {} on line {} in {}", element, filename, lineno);
      }
    }

    @Override
    public void edit(MethodCall invo) throws CannotCompileException {
      _edit(invo, invo.getMethodName());
    }

    @Override
    public void edit(ConstructorCall cc) throws CannotCompileException {
      _edit(cc, "<init>");
    }

    @Override
    public void edit(NewExpr newExpr) throws CannotCompileException {
      _edit(newExpr, newExpr.getClassName());
    }
  }
}