package npex.extractor.invocation;

import org.json.JSONObject;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtTypeReference;

public class InvocationKey {
  final String methodName;
  final int nullPos;
  final int actualsLength;
  final String returnType;

  public InvocationKey(CtInvocation invo, int nullPos) {
    this.methodName = invo.getExecutable().getSimpleName();
    this.nullPos = nullPos;
    this.actualsLength = invo.getArguments().size();
    this.returnType = abstractReturnType(invo.getType());

  }

  static private String abstractReturnType(CtTypeReference type) {
    if (type == null)
      return "void";
    else if (type.isPrimitive())
      return type.toString();
    else if (type.toString().equals("java.lang.String"))
      return "java.lang.String";
    else
      return "object";
  }

  public JSONObject toJSON() {
    var obj = new JSONObject();
    obj.put("method_name", methodName);
    obj.put("null_pos", nullPos);
    obj.put("actuals_length", actualsLength);
    obj.put("return_type", returnType);
    return obj;
  }
}