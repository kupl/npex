package npex.extractor.invocation;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;

import npex.common.NPEXException;
import spoon.SpoonException;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtTypeReference;

public class InvocationKey {
  final public String methodName;
  final public int nullPos;
  final public int actualsLength;
  final public String returnType;
  final public String invoKind;

  private InvocationKey(CtAbstractInvocation invo, int nullPos) throws NPEXException {
    this.methodName = invo.getExecutable().getSimpleName();
    this.nullPos = nullPos;
    this.actualsLength = invo.getArguments().size();
    try {
      this.returnType = abstractReturnType(invo.getExecutable().getType());
    } catch (SpoonException e) {
      throw new NPEXException("Failed to create invocation key: could not print type name");
    }
    this.invoKind = getInvoKind(invo);
  }

  static public InvocationKey createKey(CtAbstractInvocation invo, CtExpression nullExp) throws NPEXException {
    if (invo instanceof CtInvocation virtualInvo && virtualInvo.getTarget().equals(nullExp)) {
      return new InvocationKey(invo, -1);
    }
    int nullPos = invo.getArguments().indexOf(nullExp);
    if (nullPos == -1) {
      throw new NPEXException(String.format("Could not find null expr %s in invocation %s", nullExp, invo));
    }
    return new InvocationKey(invo, nullPos);
  }

  static public List<InvocationKey> enumerateKeys(CtAbstractInvocation invo) throws NPEXException {
    List<InvocationKey> keys = new ArrayList<>();
    int begin = invo instanceof CtInvocation vInvo && !vInvo.getExecutable().isStatic() ? -1 : 0;
    for (int nullPos = begin; nullPos < invo.getArguments().size(); nullPos++) {
      keys.add(new InvocationKey(invo, nullPos));
    }
    return keys;
  }

  static private String getInvoKind(CtAbstractInvocation invo) {
    if (invo.getExecutable().isConstructor()) {
      return "CONSTRUCTOR";
    } else if (invo.getExecutable().isStatic()) {
      return "STATIC";
    } else {
      return "VIRTUAL";
    }
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
    obj.put("invo_kind", invoKind);
    return obj;
  }
}