package npex.extractor.invocation;

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import npex.common.NPEXException;
import spoon.SpoonException;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

public class MethodSignature {
  final public String returnType;
  final public List<String> argTypes;
  final public String methodName;
  public MethodSignature(CtTypeReference type, CtAbstractInvocation invo, int nullPos) throws NPEXException {
    final CtExecutableReference exec = invo.getExecutable();
    try {
      this.methodName = exec.getExecutableDeclaration().getSignature();
      this.returnType = type.toString();
      this.argTypes = (List<String>) exec.getExecutableDeclaration().getParameters().stream()
          .map(x -> ((CtParameter) x).getType().toString()).collect(Collectors.toList());
    } catch (NullPointerException | SpoonException e) {
      throw new NPEXException(invo, "Failed to create method signature: could not print type name");
    }
  }

  public JSONObject toJSON() {
    var obj = new JSONObject();
    obj.put("arg_types", argTypes);
    obj.put("method_name", methodName);
    obj.put("return_type", returnType);
		return obj;
  }
}