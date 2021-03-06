package npex.extractor.invocation;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import npex.common.NPEXException;
import npex.common.utils.FactoryUtils;
import spoon.SpoonException;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import npex.common.helper.TypeHelper;

public class InvocationKey {
  final public String methodName;
  final public int nullPos;
  final public int actualsLength;
  final public String rawReturnType;
  final public String returnType;
  final public String invoKind;
  final public boolean calleeDefined;

  private InvocationKey(CtAbstractInvocation invo, int nullPos) throws NPEXException {
    CtTypeReference type = TypeHelper.getTypeOfInvocation(invo);
    if (type == null) {
      throw new NPEXException("Failed to create invocation key: return type is null");
    }

    final CtExecutableReference exec = invo.getExecutable();
    this.methodName = exec.getSimpleName();
    this.nullPos = nullPos;
    this.actualsLength = invo.getArguments().size();
    try {
      this.rawReturnType = type.toString();
      this.returnType = abstractReturnType(type);
    } catch (SpoonException e) {
      throw new NPEXException("Failed to create invocation key: could not print type name");
    }
    this.invoKind = getInvoKind(invo);
    this.calleeDefined = exec.getExecutableDeclaration() != null;
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
      try {
        InvocationKey key = new InvocationKey(invo, nullPos);
        keys.add(key);
      } catch (NPEXException e) {
        continue;
      }
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
    if (type.equals(FactoryUtils.VOID_TYPE))
      return FactoryUtils.VOID_TYPE.toString();
    else if (type.isPrimitive())
      return type.toString();
    else if (type.equals(FactoryUtils.STRING_TYPE))
      return "java.lang.String";
    else if (type.equals(FactoryUtils.OBJECT_TYPE))
      return "java.lang.Object";
    else if (type.isSubtypeOf(FactoryUtils.COLLECTION_TYPE))
      return "java.util.Collection";
    else if (type.isSubtypeOf(FactoryUtils.CLASS_TYPE))
      return "java.lang.Class";
    else
      return "OTHERS";

  }

  public JSONObject toJSON() {
    var obj = new JSONObject();
    obj.put("method_name", methodName);
    obj.put("null_pos", nullPos);
    obj.put("actuals_length", actualsLength);
    obj.put("raw_return_type", rawReturnType);
    obj.put("return_type", returnType);
    obj.put("invo_kind", invoKind);
    obj.put("callee_defined", calleeDefined);
    return obj;
  }
}