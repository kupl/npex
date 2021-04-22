package npex.extractor;

import org.json.JSONObject;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtTypeReference;

public class InvocationKey {
  public final String methodName;
  public final int nullPos;
  public final int actualsLen;
  public final String return_type;

  public InvocationKey(CtInvocation invo, int nullPos) {
    this.methodName = invo.getExecutable().getSimpleName();
    this.nullPos = nullPos;
    this.actualsLen = invo.getArguments().size();
    this.return_type = abstractReturnType(invo.getType());

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
    obj.put("nullPos", nullPos);
    obj.put("actuals_length", actualsLen);
    obj.put("return_type", return_type);
    return obj;
  }
}