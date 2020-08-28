package npex;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import org.json.JSONObject;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.filter.NamedElementFilter;
import spoon.reflect.visitor.filter.TypeFilter;

public class NPEInfo {
  String filepath;
  int line;
  String deref_field;
  CtMethod<?> sink_method;
  CtClass<?> sink_class;

  public static NPEInfo readFromJSON(Factory factory, String jsonPath) throws IOException {
    String contents = new String(Files.readAllBytes(Paths.get(jsonPath)));
    JSONObject js = new JSONObject(contents);
    String filepath = js.getString("filepath");
    int line = js.getInt("line");
    String deref_field = js.getString("deref_field");
    String sink_class_name = js.getString("sink_class");
    CtClass<?> sink_class = factory.getModel().getElements(new NamedElementFilter<>(CtClass.class, sink_class_name))
        .get(0);
    CtMethod<?> sink_method = null;
    return new NPEInfo(filepath, line, deref_field, sink_method, sink_class);
  }

  public NPEInfo(String filepath, int line, String deref_field, CtMethod<?> sink_method, CtClass<?> sink_class) {
    this.filepath = filepath;
    this.line = line;
    this.deref_field = deref_field;
    this.sink_method = sink_method;
    this.sink_class = sink_class;
  }

  public void writeToJSON(File outFile) throws IOException {
    JSONObject js = new JSONObject();
    js.put("filepath", filepath);
    js.put("line", line);
    js.put("deref_field", deref_field);
    js.put("sink_method", sink_method.getSimpleName());
    js.put("sink_class", sink_class.getSimpleName());

    FileWriter writer = new FileWriter(outFile);
    writer.write(js.toString(4));
    writer.close();
  }

  public CtExpression<?> resolve() throws NoSuchElementException {
    for (CtExpression<?> expr : sink_class.getElements(new TypeFilter<>(CtExpression.class))) {
      if (!expr.getPosition().isValidPosition() || expr.getPosition().getLine() != line)
        continue;

      if (expr instanceof CtVariableRead
          && deref_field.equals(((CtVariableRead<?>) expr).getVariable().getSimpleName()))
        return expr;

      if (expr instanceof CtFieldAccess
          && deref_field.equals(((CtFieldAccess<?>) expr).getVariable().getSimpleName())) {
        return expr.getParent(CtTargetedExpression.class).getTarget();
      }

      if (expr instanceof CtInvocation) {
        if (((CtInvocation<?>) expr).getExecutable().getSignature().contains(deref_field))
          return expr;
        continue;
      }
    }

    throw new NoSuchElementException();
  }

  public static String resolveDerefField(CtExpression<?> nullExp) throws IllegalArgumentException {
    if (nullExp instanceof CtVariableRead) {
      return nullExp.toString();
    }

    if (nullExp instanceof CtFieldAccess) {
      return nullExp.getParent(CtTargetedExpression.class).getTarget().toString();
    }

    if (nullExp instanceof CtInvocation) {
      return ((CtInvocation<?>) nullExp).getExecutable().toString();
    }

    throw new IllegalArgumentException();
  }
}