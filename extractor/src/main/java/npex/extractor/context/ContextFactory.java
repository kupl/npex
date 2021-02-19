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

import java.util.ArrayList;
import java.util.List;

public class ContextFactory {
  private final static List<Context> contexts = new ArrayList<>();
  static {
    contexts.add(new NullCheckingExists());
    contexts.add(new UsedAsArgument());
    contexts.add(new UsedAsReturnExpression());
    contexts.add(new IsParameter());
    contexts.add(new IsVariable());
    contexts.add(new IsField());
    contexts.add(new SinkExprIsAssigned());
    contexts.add(new SinkMethodIsConstructor());
    contexts.add(new SinkMethodIsPrivate());
    contexts.add(new SinkMethodIsPublic());
    contexts.add(new SinkMethodIsStatic());
  }

  public static List<Context> getAllContexts() {
    return contexts;
  }
}