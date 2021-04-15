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
package npex.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.json.JSONObject;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtTargetedExpression;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;
import spoon.reflect.visitor.filter.NamedElementFilter;

public class NPEInfo {
  String filepath;
  int line;
  String deref_field;
  CtMethod<?> npe_method;
  CtClass<?> npe_class;
  CtMethod<?> callee;

  public static NPEInfo readFromJSON(CtModel model, String jsonPath) throws IOException, NoSuchElementException {
    JSONObject js = new JSONObject(new String(Files.readAllBytes(Paths.get(jsonPath))));
    String filepath = js.getString("filepath");
    int line = js.getInt("line");
    String deref_field = js.getString("deref_field");
    String npe_class_name = js.getString("npe_class");
    CtClass<?> npe_class = model.getElements(new NamedElementFilter<>(CtClass.class, npe_class_name)).stream()
        .filter(c -> c.getPosition().getFile().toString().contains(filepath)).findFirst().get();

    CtMethod<?> npe_method = null;
    return new NPEInfo(filepath, line, deref_field, npe_method, npe_class);
  }

  public NPEInfo(String filepath, int line, String deref_field, CtMethod<?> npe_method, CtClass<?> npe_class) {
    this.filepath = filepath;
    this.line = line;
    this.deref_field = deref_field;
    this.npe_method = npe_method;
    this.npe_class = npe_class;
  }

  public void writeToJSON(File outFile) throws IOException {
    JSONObject js = new JSONObject();
    js.put("filepath", filepath);
    js.put("line", line);
    js.put("deref_field", deref_field);
    js.put("npe_method", npe_method.getSimpleName());
    js.put("npe_class", npe_class.getSimpleName());

    FileWriter writer = new FileWriter(outFile);
    writer.write(js.toString(4));
    writer.close();
  }

  public CtExpression<?> resolve() throws NoSuchElementException {
    NPEScanner scanner = new NPEScanner();
    npe_class.accept(scanner);

    for (CtExpression expr : scanner.getExpressions()) {
      if (expr instanceof CtVariableRead
          && deref_field.equals(((CtVariableRead<?>) expr).getVariable().getSimpleName()))
        return expr;

      if (expr instanceof CtFieldAccess
          && deref_field.equals(((CtFieldAccess<?>) expr).getVariable().getSimpleName())) {
        return expr.getParent(CtTargetedExpression.class).getTarget();
      }

      if (expr instanceof CtInvocation invo) {
        if (invo.getExecutable().getSignature().contains(deref_field))
          return expr;
        continue;
      }

      if (expr instanceof CtLiteral) {
        if (deref_field.equals("null") && ((CtLiteral<?>) expr).toString().equals("null"))
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

  private class NPEScanner extends CtScanner {
    private Set<CtExpression> expressions = new HashSet<>();

    private boolean isLineMatched(CtElement e) {
      return e.getPosition().isValidPosition() && e.getPosition().getLine() == line;
    }

    public Set<CtExpression> getExpressions() {
      return expressions;
    }

    @Override
    public void scan(CtElement e) {
      super.scan(e);
      if (e instanceof CtExpression && isLineMatched(e)) {
        expressions.add((CtExpression) e);
      }
    }

  }
}
