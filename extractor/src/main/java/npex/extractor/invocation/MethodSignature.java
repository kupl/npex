package npex.extractor.invocation;

import java.io.Serializable;

import org.json.JSONObject;

import npex.common.NPEXException;
import spoon.SpoonException;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;

public class MethodSignature implements Serializable {
  final public String methodName;
  final public String returnType;
  final public int nullPos;

  @Override
  public boolean equals(Object o) {
    return o instanceof MethodSignature m ? this.toString().equals(m.toString()) : false;
  }

  @Override
  public int hashCode() {
   return toString().hashCode(); 
  }

  public MethodSignature(CtExecutable exec) throws NPEXException {
    this(exec, -1);
  }

  public MethodSignature(CtAbstractInvocation invo, int nullPos) throws NPEXException {
    this(invo.getExecutable().getExecutableDeclaration(), nullPos);
  }

  public MethodSignature(CtExecutable exec, int nullPos) throws NPEXException {
    try {
      this.methodName = exec.getSignature();
      this.returnType = exec.getType().toString();
      this.nullPos = nullPos;
    } catch (NullPointerException | SpoonException e) {
      throw new NPEXException(exec, "Failed to create method signature: could not print type name");
    }
  }
  public JSONObject toJSON() {
    var obj = new JSONObject();
    obj.put("method_name", methodName);
    obj.put("return_type", returnType);
    obj.put("null_pos", nullPos);
		return obj;
  }

  public String toString() {
    return this.methodName + this.nullPos+ this.returnType;
  }

}