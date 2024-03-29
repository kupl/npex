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
package npex.synthesizer.initializer;

import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtNewArray;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

@SuppressWarnings("rawtypes")
public class ObjectInitializer extends ValueInitializer<CtConstructorCall> {
  static final HashMap<CtTypeReference, String> collectionsMap = new HashMap<>();
  static {
    collectionsMap.put(tf.LIST, "java.util.ArrayList");
    collectionsMap.put(tf.SET, "java.util.HashSet");
    collectionsMap.put(tf.MAP, "java.util.HashMap");
  }

  public String getName() {
    return "Object";
  }

  protected CtExpression convertToCtExpression(CtConstructorCall ctor) {
    if (ctor.getType().isArray()) {
      CtNewArray arr = ctor.getFactory().createNewArray();
      arr.setType(ctor.getType());
      return arr;
    }
    return ctor;
  }

  protected Stream<CtConstructorCall> enumerate(CtExpression expr) {
    CtTypeReference typ = expr.getType();
    Factory factory = expr.getFactory();

    if (typ == null) {
      return Stream.empty();
    }

    CtTypeReference ctype = collectionsMap.keySet().stream().filter(ct -> typ.isSubtypeOf(ct)).findAny().orElse(null);
    if (ctype != null) {
      CtType typeDecl = typ.getTypeDeclaration();
      String implTypName = (typeDecl instanceof CtClass impl && !impl.isAbstract()) ? typeDecl.getQualifiedName()
          : collectionsMap.get(ctype);

      String codeSnippetStr;
      if (typ.isGenerics()) {
        String typeParams = factory.getEnvironment().getComplianceLevel() >= 8 ? ""
            : typ.getActualTypeArguments().stream().map(ty -> ty.getQualifiedName()).collect(Collectors.joining(", "));
        codeSnippetStr = String.format("new %s<%s>()", implTypName, typeParams);
      } else {
        codeSnippetStr = String.format("new %s()", implTypName);
      }

      CtExpression ctor = factory.createCodeSnippetExpression(String.format("new %s()", implTypName)).compile();

      return Collections.singleton((CtConstructorCall) ctor).stream();
    }

    if (!typ.isClass() || typ.isPrimitive() || typ.isInterface()
        || typ.getDeclaration() != null && typ.getDeclaration().isAbstract()) {
      return Stream.empty();
    }

    if (typ instanceof CtArrayTypeReference) {
      return Collections.singleton(expr.getFactory().createConstructorCall(typ)).stream();
    }

    CtType tyDecl = typ.getDeclaration();
    if (tyDecl instanceof CtClass klass && klass.getConstructor() != null) {
      return Collections.singleton(expr.getFactory().createConstructorCall(typ)).stream();
    }

    return Stream.empty();
  }

  public static Stream<CtConstructorCall> enumerate(CtTypeReference typ) {
     if (typ == null) {
      return Stream.empty();
    }
    Factory factory = typ.getFactory();

    CtTypeReference ctype = collectionsMap.keySet().stream().filter(ct -> typ.isSubtypeOf(ct)).findAny().orElse(null);
    if (ctype != null) {
      CtType typeDecl = typ.getTypeDeclaration();
      String implTypName = (typeDecl instanceof CtClass impl && !impl.isAbstract()) ? typeDecl.getQualifiedName()
          : collectionsMap.get(ctype);

      String codeSnippetStr;
      if (typ.isGenerics()) {
        String typeParams = factory.getEnvironment().getComplianceLevel() >= 8 ? ""
            : typ.getActualTypeArguments().stream().map(ty -> ty.getQualifiedName()).collect(Collectors.joining(", "));
        codeSnippetStr = String.format("new %s<%s>()", implTypName, typeParams);
      } else {
        codeSnippetStr = String.format("new %s()", implTypName);
      }

      CtExpression ctor = factory.createCodeSnippetExpression(String.format("new %s()", implTypName)).compile();

      return Collections.singleton((CtConstructorCall) ctor).stream();
    }

    if (!typ.isClass() || typ.isPrimitive() || typ.isInterface()
        || typ.getDeclaration() != null && typ.getDeclaration().isAbstract()) {
      return Stream.empty();
    }

    if (typ instanceof CtArrayTypeReference) {
      return Collections.singleton(typ.getFactory().createConstructorCall(typ)).stream();
    }

    CtType tyDecl = typ.getDeclaration();
    if (tyDecl instanceof CtClass klass && klass.getConstructor() != null) {
      return Collections.singleton(typ.getFactory().createConstructorCall(typ)).stream();
    }

    return Stream.empty();
  }

}