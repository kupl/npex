package npex.extractor.invocation;

import org.json.JSONObject;

import spoon.reflect.code.CtAbstractInvocation;

public class InvocationSite {
  public final String sourcePath;
  public final int lineno;
  public final String derefField;

  public InvocationSite(CtAbstractInvocation invo) {
    this.sourcePath = invo.getPosition().getFile().toString();
    this.lineno = invo.getPosition().getLine();
    this.derefField = invo.getExecutable().getSimpleName();
  }

  public JSONObject toJSON() {
    var obj = new JSONObject();
    obj.put("source_path", sourcePath);
    obj.put("lineno", lineno);
    obj.put("deref_field", derefField);
    return obj;
  }
}