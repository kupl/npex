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

import npex.common.NPEXException;
import npex.common.utils.FactoryUtils;
import npex.extractor.context.ContextExtractor;
import npex.extractor.invocation.InvocationKey;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.EarlyTerminatingScanner;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtConstructorCall;

public class NullModel {
  static Logger logger = LoggerFactory.getLogger(NullModel.class);
  private final CtExpression nullExp;
  private final CtElement sinkBody;
  private final CtExpression nullValue;
  private final boolean isThrow;
  private final CtAbstractInvocation invo;
  private final InvocationKey invoKey;

  final private Map<String, Boolean> contexts;

  public NullModel(CtExpression nullExp, CtElement sinkBody, CtExpression nullValue, boolean isThrow) {
    this.nullExp = nullExp;
    this.sinkBody = sinkBody;
    this.nullValue = nullValue;
    this.isThrow = isThrow;
    NullInvocationScanner scanner = new NullInvocationScanner();
    sinkBody.accept(scanner);
    this.invo = scanner.getResult();
    this.invoKey = invo != null ? InvocationKey.createKey(invo, nullExp) : null;
    this.contexts = invoKey != null ? ContextExtractor.extract(invo, invoKey.nullPos) : null;
  }

  public NullModel(CtExpression nullExp, CtElement sinkBody, CtExpression nullValue) {
    this(nullExp, sinkBody, nullValue, false);
  }

  public JSONObject toJSON() throws NPEXException {
    if (invoKey == null) {
      throw new NPEXException(
          String.format("Could not serialize null model at %s: invocation key is NULL", nullExp.getPosition()));
    }

    var obj = new JSONObject();
    obj.put("sink_body", sinkBody.toString());
    obj.put("null_value", abstractNullValue(nullValue));
    obj.put("is_throw", isThrow);
    obj.put("actual_null_value", nullValue != null ? nullValue.toString() : JSONObject.NULL);
    obj.put("invocation_key", invoKey.toJSON());
    obj.put("contexts", new JSONObject(contexts));
    return obj;
  }

  protected String abstractNullValue(CtExpression nullValue) throws NPEXException {
    if (nullValue == null) {
      throw new NPEXException(
          String.format("Cannot extract null values for model %s invoaction infomation is incomplete!", this));
    }
    CtTypeReference valueType = nullValue.getType();
    CtTypeReference invoRetType = invo.getExecutable().getType();

    if (valueType == null) {
      throw new NPEXException(String.format("Cannot extract null values for model %s its value type is null", this));
    }

    if (invoRetType == null) {
      throw new NPEXException(
          String.format("Cannot extract null values for model %s return type of invocation is null", this));
    }

    if (valueType.isGenerics()) {
      throw new NPEXException(
          String.format("Cannot extract null values for model %s its value type is generics", this));
    }

    if (!nullValue.toString().equals("null") && !valueType.isSubtypeOf(invoRetType)) {
      throw new NPEXException(String.format("Cannot extract null values for model %s: %s is not a subtype of %s", this,
          valueType, invoRetType));
    }

    if (nullValue.toString().equals("null") && invoRetType.isPrimitive()) {
      throw new NPEXException(String
          .format("Invocation's return type is primitive but null literal is collected as null value for %s", this));
    }

    if (nullValue instanceof CtLiteral) {
      return nullValue.toString();
    } else {
      return "NPEXNonLiteral";
    }
  }

  private class NullInvocationScanner extends EarlyTerminatingScanner<CtAbstractInvocation> {

    @Override
    public void visitCtInvocation(CtInvocation invo) {
      super.visitCtInvocation(invo);
      if (invo.getTarget() == nullExp || invo.getArguments().contains(nullExp)) {
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