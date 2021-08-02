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

import java.util.Collections;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.common.NPEXException;
import npex.extractor.invocation.InvocationKey;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.EarlyTerminatingScanner;

public class NullModel {
  static Logger logger = LoggerFactory.getLogger(NullModel.class);
  private final CtExpression nullExp;
  private final CtElement sinkBody;
  private final CtAbstractInvocation invo;
  private final InvocationKey invoKey;
  private final NullValue nullValue;

  final private Map<String, Boolean> contexts;

  public NullModel(CtExpression nullExp, CtElement sinkBody, NullValue nullValue) {
    this.nullExp = nullExp;
    this.sinkBody = sinkBody;
    NullInvocationScanner scanner = new NullInvocationScanner();
    sinkBody.accept(scanner);
    this.invo = scanner.getResult();
    this.invoKey = invo != null ? InvocationKey.createKey(invo, nullExp) : null;
    this.contexts = invoKey != null ? invoKey.extract() : null;
    this.nullValue = nullValue;
  }

  public JSONObject toJSON() throws NPEXException {
    if (nullValue != null && nullValue.isNotToLearn()) {
      var obj = new JSONObject();
      obj.put("sink_body", JSONObject.NULL);
      obj.put("null_value", nullValue.toJSON());
      obj.put("invocation_key", JSONObject.NULL);
      obj.put("contexts", new JSONObject(Collections.EMPTY_MAP));
      return obj;
    }

    if (invoKey == null) {
      throw new NPEXException(nullExp, "Could not serialize null model: invocation key is NULL");
    }

    if (nullValue == null) {
      throw new NPEXException(nullExp, "Could not serialize null model: null value is NULL");
    }

    var obj = new JSONObject();
    obj.put("sink_body", sinkBody.toString());
    obj.put("null_value", nullValue.toJSON());
    obj.put("invocation_key", invoKey.toJSON());
    obj.put("contexts", new JSONObject(contexts));
    return obj;
  }

  private class NullInvocationScanner extends EarlyTerminatingScanner<CtAbstractInvocation> {
    @Override
    public void visitCtInvocation(CtInvocation invo) {
      super.visitCtInvocation(invo);
      if (nullExp.equals(invo.getTarget()) || invo.getArguments().contains(nullExp)) {
        setResult(invo);
        terminate();
      }
    }

    @Override
    public void visitCtConstructorCall(CtConstructorCall invo) {
      super.visitCtConstructorCall(invo);
      if (invo.getArguments().contains(nullExp)) {
        setResult(invo);
        terminate();
      }
    }
  }
}