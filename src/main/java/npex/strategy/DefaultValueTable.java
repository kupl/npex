package npex.strategy;

import org.apache.commons.lang3.NotImplementedException;

import spoon.reflect.reference.CtTypeReference;

public final class DefaultValueTable {
  public DefaultValueTable() {

  }

  static String getValueString(CtTypeReference<?> typ) {
    String typName = typ.getSimpleName();
    switch (typName) {
      case "void":
        return "";
      case "int":
        return "1";
      case "String":
        return "\"\"";
      case "boolean":
        return "false";
      case "double":
      case "float":
        return "0.0";
      default:
        throw new NotImplementedException("No default expression for typ: " + typName);
    }
  }

}
