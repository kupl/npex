package npex;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

import spoon.reflect.code.CtForEach;
import spoon.reflect.code.CtLoop;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

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

  public static <T extends CtElement> T findMatchedElement(CtElement at, CtElement element)
      throws NoSuchElementException {
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