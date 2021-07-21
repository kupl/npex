package npex.extractor.invocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import npex.common.NPEXException;
import npex.common.helper.TypeHelper;
import npex.common.utils.TypeUtil;
import npex.extractor.context.ContextExtractor;
import npex.extractor.runtime.RuntimeMethodInfo;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

public class InvocationKey {
  final public MethodSignature methodSignature;
  final public String methodName; // simple name
  final public int nullPos;
  final public int actualsLength;
  final public String returnType;
  final public String invoKind;
  final public boolean isRuntimeCallee;
  final public boolean calleeDefined;
	final public CtAbstractInvocation invo;

    private InvocationKey(CtAbstractInvocation invo, int nullPos) throws NPEXException {
      CtTypeReference type = TypeHelper.getType(invo);
      if (type == null) {
        throw new NPEXException(invo, "Failed to create invocation key: return type is null");
      }
      final CtExecutableReference exec = invo.getExecutable();

      this.methodSignature = new MethodSignature(invo, nullPos);
      this.methodName = exec.getSimpleName();
      this.nullPos = nullPos;
      this.actualsLength = invo.getArguments().size();
      this.returnType = abstractReturnType(type);
      this.invoKind = getInvoKind(invo);
      this.isRuntimeCallee = RuntimeMethodInfo.isRuntimeCallee(methodSignature);
      boolean calleeDefined;
      try {
        calleeDefined = isRuntimeCallee ? true : !exec.getExecutableDeclaration().getBody().getStatements().isEmpty();
      } catch (NullPointerException e) {
        calleeDefined = false;
      }
      this.calleeDefined = calleeDefined;
      this.invo = invo;
    }

	static public InvocationKey createKey(CtAbstractInvocation invo, CtExpression nullExp) throws NPEXException {
    if (invo instanceof CtInvocation virtualInvo && virtualInvo.getTarget().equals(nullExp)) {
      return new InvocationKey(invo, -1);
    }
    int nullPos = invo.getArguments().indexOf(nullExp);
    if (nullPos == -1) {
      throw new NPEXException(invo, String.format("Could not find null expr %s in invocation %s", nullExp, invo));
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
    if (type.equals(TypeUtil.VOID))
      return TypeUtil.VOID.toString();
    else if (type.isPrimitive())
      return type.toString();
    else if (type.equals(TypeUtil.STRING))
      return "java.lang.String";
    else if (type.equals(TypeUtil.OBJECT))
      return "java.lang.Object";
    else if (type.isSubtypeOf(TypeUtil.COLLECTION))
      return "java.util.Collection";
    else if (type.isSubtypeOf(TypeUtil.CLASS))
      return "java.lang.Class";
    else
      return "OTHERS";
  }

  public JSONObject toJSON() {
    var obj = new JSONObject();
    obj.put("method_signature", methodSignature.toJSON());
    obj.put("method_name", methodName);
    obj.put("null_pos", nullPos);
    obj.put("actuals_length", actualsLength);
    obj.put("return_type", returnType);
    obj.put("invo_kind", invoKind);
    obj.put("callee_defined", calleeDefined);
    return obj;
  }

  public Map<String, Boolean> extract() {
    return ContextExtractor.extract(this);
  }
}