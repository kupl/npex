package npex.extractor.invocation;

import java.io.Serializable;

import org.json.JSONObject;

import npex.common.helper.TypeHelper;
import spoon.SpoonException;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

public class MethodSignature implements Serializable {
  final public String methodName;
  // final public String[] argTypes;
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

  private MethodSignature(String signature, CtTypeReference returnType, int nullPos) throws SpoonException {
    this.methodName = signature;
    this.returnType = returnType.toString();
    this.nullPos = nullPos;
  }

  public MethodSignature(CtAbstractInvocation invo, int nullPos) {
    this(invo.getExecutable().getSignature(),  TypeHelper.getType(invo), nullPos);
  }
  public MethodSignature(CtExecutableReference exec, int nullPos) {
    this(exec.getSignature(), exec.getType(), nullPos);
  }

  public MethodSignature(CtExecutable exec, int nullPos) {
    this(exec.getReference(), nullPos);
  }

  public MethodSignature(CtExecutable exec)  {
    this(exec.getReference(), -1);
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