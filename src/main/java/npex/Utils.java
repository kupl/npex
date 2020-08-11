package npex;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

import org.json.JSONObject;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;

public class Utils {
  public static CtStatement getEnclosingStatement(CtElement el) {
    while (el != null) {
      CtStatement parent = el.getParent(CtStatement.class);
      if (parent == null) {
        return (CtStatement) el;
      }
      if (parent.getPosition().getLine() != el.getPosition().getLine())
        return (CtStatement) el;
      el = parent;
    }
    throw new IllegalArgumentException("this should not happen");
  }

  public static CtExpression<?> resolveNullPointer(Factory factory, String jsonPath)
      throws IOException, NoSuchElementException {
    String contents = new String(Files.readAllBytes(Paths.get(jsonPath)));
    JSONObject jsonObject = new JSONObject(contents);
    int line = jsonObject.getInt("line");
    String sinkClassName = jsonObject.getString("npe_class");
    String derefField = jsonObject.getString("deref_field");
    CtClass<?> klass = factory.getModel().getElements(new NamedElementFilter<>(CtClass.class, sinkClassName)).get(0);
    System.out.println(klass.getSimpleName());
    System.out.println(klass);
    for (CtExpression<?> expr : klass.getElements(new TypeFilter<>(CtExpression.class))) {
      if (!expr.getPosition().isValidPosition() || expr.getPosition().getLine() != line)
        continue;

      if (expr instanceof CtVariableAccess
          && derefField.equals(((CtVariableAccess<?>) expr).getVariable().getSimpleName()))
        return expr;

      if (expr instanceof CtFieldAccess && derefField.equals(((CtFieldAccess<?>) expr).getVariable().getSimpleName())) {
        return expr.getParent(CtTargetedExpression.class).getTarget();
      }

      if (expr instanceof CtInvocation) {
        if (((CtInvocation<?>) expr).getExecutable().getSignature().contains(derefField))
          return expr;
        continue;
      }
    }

    throw new NoSuchElementException();
  }

  public static <T extends CtElement> T findMatchedElement(CtElement at, CtElement element)
      throws NoSuchElementException {
    System.out.println(element);
    return (T) StreamSupport.stream(at.asIterable().spliterator(), false).filter(x -> x.equals(element)).findFirst()
        .get();
  }

  public static File getSourceFile(CtElement element) {
    return element.getFactory().CompilationUnit().getOrCreate(element.getParent(CtClass.class)).getFile();
  }

  public static CtElement getLoopHeadElement(CtForEach loop) {
    return loop.getExpression();
  }

  public static CtElement getLoopHeadElement(CtWhile loop) {
    return loop.getLoopingExpression();
  }

  public static CtElement getLoopHeadElement(CtLoop loop) {
    if (loop instanceof CtForEach) {
      return getLoopHeadElement((CtForEach) loop);
    }
    if (loop instanceof CtWhile) {
      return getLoopHeadElement((CtWhile) loop);
    }
    return null;
  }
}