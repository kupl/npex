package npex.extractor.nullhandle;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtTypeReference;

public record InvocationInfo(int nullIdx, CtInvocation orgInvo, CtInvocation nullInvo) {
  static private enum INVO_KIND {
    CONSTRUCTOR, STATIC, VIRTUAL
  }

  static InvocationInfo createNullBaseInvocationInfo(CtInvocation orgInvo, CtInvocation nullInvo) {
    return new InvocationInfo(-1, orgInvo, nullInvo);
  }

  static InvocationInfo createNullArgumentInvocationInfo(int idx, CtInvocation orgInvo, CtInvocation nullInvo) {
    return new InvocationInfo(idx, orgInvo, nullInvo);
  }

  public JSONObject toJSON() {
    var obj = new JSONObject();
    CtTypeReference targetType = getInvocationType().equals(INVO_KIND.VIRTUAL) ? orgInvo.getTarget().getType() : null;
    obj.put("null_invo", nullInvo);
    obj.put("null_idx", nullIdx);
    obj.put("method_name", nullInvo.getExecutable().getSimpleName());
    obj.put("return_type", nullInvo.getType() != null ? nullInvo.getType().toString() : JSONObject.NULL);
    obj.put("arguments_types",
        new JSONArray(getActualArgumentsTypes().stream().map(argTyp -> argTyp.toString()).toArray()));
    obj.put("invo_kind", getInvocationType().toString());
    obj.put("target_type", targetType != null ? targetType : JSONObject.NULL);
    return obj;
  }

  private INVO_KIND getInvocationType() {
    if (orgInvo.getExecutable().isConstructor())
      return INVO_KIND.CONSTRUCTOR;
    else if (nullInvo.getExecutable().isStatic())
      return INVO_KIND.STATIC;
    else
      return INVO_KIND.VIRTUAL;
  }

  private List<CtTypeReference> getActualArgumentsTypes() {
    Stream<CtExpression> argsStream = orgInvo.getArguments().stream();
    List<CtTypeReference> typs = argsStream.map(arg -> arg.getType()).collect(Collectors.toList());
    return typs;
  }
}