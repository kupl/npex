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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.utils.FactoryUtils;
import npex.extractor.context.ContextExtractor;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.EarlyTerminatingScanner;

public class NullModel {
  static Logger logger = LoggerFactory.getLogger(NullModel.class);
  private final CtExpression nullExp;
  private final CtElement sinkBody;
  private final CtExpression nullValue;
  private final InvocationInfo invoInfo;

  final private Map<String, Boolean> contexts;

  public NullModel(CtExpression nullExp, CtElement sinkBody, CtExpression nullValue) {
    this.nullExp = nullExp;
    this.sinkBody = sinkBody;
    this.nullValue = nullValue;

    NullInvocationScanner scanner = new NullInvocationScanner();
    sinkBody.accept(scanner);
    this.invoInfo = scanner.getResult();
    this.contexts = invoInfo != null ? ContextExtractor.extract(invoInfo.orgInvo(), invoInfo.nullIdx()) : null;
  }

  public JSONObject toJSON() {
    var obj = new JSONObject();
    obj.put("sink_body", sinkBody.toString());
    obj.put("null_value", nullValue != null ? abstractNullValue(nullValue) : JSONObject.NULL);
    obj.put("actual_null_value", nullValue != null ? nullValue.toString() : JSONObject.NULL);
    obj.put("invocation_info", invoInfo != null ? invoInfo.toJSON() : JSONObject.NULL);
    obj.put("contexts", contexts != null ? new JSONObject(contexts) : JSONObject.NULL);
    return obj;
  }

  private String abstractNullValue(CtExpression nullValue) {
    if (nullValue instanceof CtLiteral) {
      return nullValue.toString();
    } else {
      return "NPEXNonLiteral";
    }
  }

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
      nullInvo.getTarget().replace(FactoryUtils.createNullLiteral());
      setResult(InvocationInfo.createNullBaseInvocationInfo(invo, nullInvo));
      terminate();
    }

    private void whenActualIsNull(CtInvocation invo) {
      if (invo.getExecutable() instanceof CtExecutableReference exec && exec.getActualMethod() instanceof CtMethod mthd
          && mthd.getReturnType())
        ;


      CtInvocation nullInvo = invo.clone();
      Stream<CtExpression> argsStream = invo.getArguments().stream();
      List<CtExpression> argsList = argsStream.map(arg -> arg.equals(nullExp) ? FactoryUtils.createNullLiteral() : arg)
          .collect(Collectors.toList());
      nullInvo.setArguments(argsList);
      int idx = invo.getArguments().indexOf(nullExp);
      setResult(InvocationInfo.createNullArgumentInvocationInfo(idx, invo, nullInvo));
      terminate();
    }

  }
}