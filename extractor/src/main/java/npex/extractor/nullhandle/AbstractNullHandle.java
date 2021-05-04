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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.CoreFactory;
import spoon.support.DefaultCoreFactory;
import npex.common.NPEXException;

public abstract class AbstractNullHandle<T extends CtElement> {
  static final protected Logger logger = LoggerFactory.getLogger(AbstractNullHandle.class);
  static final protected CoreFactory factory = new DefaultCoreFactory();

  final protected T handle;
  final protected CtBinaryOperator nullCond;
  final protected CtExpression nullExp;

  final private List<NullModel> models;

  public AbstractNullHandle(T handle, CtBinaryOperator nullCond) {
    this.handle = handle;
    this.nullCond = nullCond;
    this.nullExp = nullCond.getLeftHandOperand();
    AbstractNullModelScanner scanner = createNullModelScanner(nullExp);
    handle.accept(scanner);
    this.models = scanner.getResult();
  }

  public JSONObject toJSON() throws NPEXException {
    var obj = new JSONObject();
    obj.put("source_path", handle.getPosition().getFile().getAbsolutePath());
    obj.put("lineno", handle.getPosition().getLine());
    obj.put("handle", handle.toString());
    obj.put("models", new JSONArray(models.stream().map(m -> m.toJSON()).toArray()));
    return obj;
  }

  protected abstract AbstractNullModelScanner createNullModelScanner(CtExpression nullExp);

}