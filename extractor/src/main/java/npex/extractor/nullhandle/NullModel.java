/**
 * The MIT License
 *
 * Copyright (c) 2020 Software Analysis Laboratory, Korea University
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package npex.extractor.nullhandle;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.extractor.Utils;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.EarlyTerminatingScanner;

public class NullModel {
  static Logger logger = LoggerFactory.getLogger(NullModel.class);
  private final CtExpression nullExp;
  private final CtElement sinkBody;
  private final CtExpression nullValue;
  private final InvocationInfo invoInfo;

  public NullModel(CtExpression nullExp, CtElement sinkBody, CtExpression nullValue) {
    this.nullExp = nullExp;
    this.sinkBody = sinkBody;
    this.nullValue = nullValue;

    NullInvocationScanner scanner = new NullInvocationScanner();
    sinkBody.accept(scanner);
    this.invoInfo = scanner.getResult();
  }

  public JSONObject toJSON() {
    var obj = new JSONObject();
    obj.put("sink_body", sinkBody.toString());
    obj.put("null_value", nullValue.toString());
    obj.put("invocation_info", invoInfo != null ? invoInfo.toJSON() : JSONObject.NULL);
    return obj;
  }

  private record InvocationInfo(int nullIdx, CtInvocation orgInvo, CtInvocation nullInvo) {
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
      CtTypeReference targetType = getTargetType();
      obj.put("null_invo", nullInvo);
      obj.put("null_idx", nullIdx);
      obj.put("method_name", nullInvo.getExecutable().getSimpleName());
      obj.put("return_type", nullInvo.getType().toString());
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

    private CtTypeReference getTargetType() {
      if (getInvocationType().equals(INVO_KIND.VIRTUAL)) {
        return orgInvo.getTarget().getType();
      }
      return null;
    }

  };

  private class NullInvocationScanner extends EarlyTerminatingScanner<InvocationInfo> {
    @Override
    public void visitCtInvocation(CtInvocation invo) {
      super.visitCtInvocation(invo);
      if (invo.getTarget().equals(nullExp)) {
        whenReceiverIsNull(invo);
      } else if (invo.getArguments().contains(nullExp)) {
        whenActualIsNull(invo);
      }
    }

    private void whenReceiverIsNull(CtInvocation invo) {
      CtInvocation nullInvo = invo.clone();
      nullInvo.getTarget().replace(Utils.createNullLiteral());
      setResult(InvocationInfo.createNullBaseInvocationInfo(invo, nullInvo));
      terminate();
    }

    private void whenActualIsNull(CtInvocation invo) {
      CtInvocation nullInvo = invo.clone();
      Stream<CtExpression> argsStream = invo.getArguments().stream();
      List<CtExpression> argsList = argsStream.map(arg -> arg.equals(nullExp) ? Utils.createNullLiteral() : arg)
          .collect(Collectors.toList());
      nullInvo.setArguments(argsList);
      int idx = invo.getArguments().indexOf(nullExp);
      setResult(InvocationInfo.createNullArgumentInvocationInfo(idx, invo, nullInvo));
      terminate();
    }

  }
}