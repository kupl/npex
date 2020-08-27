package npex.strategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtExpression;
import spoon.reflect.reference.CtTypeReference;

public final class DefaultValueTable {
  static Map<String, List<String>> table = new HashMap<>();
  static {
    table.put("int", Arrays.asList(new String[] { "0", "1" }));
    table.put("String", Arrays.asList(new String[] { "null", "\"\"" }));
    table.put("boolean", Arrays.asList(new String[] { "false", "true" }));
    table.put("double", Arrays.asList(new String[] { "0.0", "1.0" }));
    table.put("float", Arrays.asList(new String[] { "0.0", "1.0" }));
    table.put("void", Arrays.asList(new String[] { "" }));
  }

  static boolean hasDefaultValue(CtTypeReference<?> typ) {
    return table.containsKey(typ.getSimpleName());
  }

  static <T> List<CtExpression<T>> getDefaultValues(CtTypeReference<T> typ) {
    if (hasDefaultValue(typ)) {
      return table.get(typ.getSimpleName()).stream().map(s -> {
        CtCodeSnippetExpression<T> exp = typ.getFactory().createCodeSnippetExpression();
        exp.setValue(s);
        return exp;
      }).collect(Collectors.toList());
    }

    return null;
  }

}
