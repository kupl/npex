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
package npex.extractor.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spoon.reflect.code.CtAbstractInvocation;

public class ContextExtractor {
  private static final List<Context> contexts = ContextFactory.getAllContexts();
  private static final List<Context> calleeContexts = ContextFactory.getCalleeContexts();

  public static Map<String, Boolean> extract(CtAbstractInvocation invo, int nullPos) {
    if (invo == null) {
      return null;
    }
    var map = new HashMap<String, Boolean>();
    contexts.forEach(ctx -> map.put(ctx.getClass().getSimpleName(), ctx.extract(invo, nullPos)));
    if (invo.getExecutable().getExecutableDeclaration() != null) {
      calleeContexts.forEach(ctx -> map.put(ctx.getClass().getSimpleName(), ctx.extract(invo, nullPos)));
    }
    return map;
  }
}
