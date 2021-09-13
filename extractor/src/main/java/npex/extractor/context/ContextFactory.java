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
  private final static List<Context> calleeContexts = new ArrayList<>();
  static {
    // AST context features
    contexts.add(new UsedAsArgument());
    contexts.add(new UsedAsOperand());
    contexts.add(new UsedAsReturnExpression());
    contexts.add(new IsParameter());
    contexts.add(new IsVariable());
    contexts.add(new LHSIsField());
    contexts.add(new LHSIsPrivate());
    contexts.add(new LHSIsPublic());
    contexts.add(new LHSIsArray());

    contexts.add(new IsField());
    contexts.add(new SinkExprIsAssigned());
    contexts.add(new SinkExprIsExceptionArgument());
    // contexts.add(new ReturnTypeHasDefault());
    // contexts.add(new CallerMethodChecksNull());
    contexts.add(new CallerMethodIsConstructor());
    contexts.add(new CallerMethodIsPrivate());
    contexts.add(new CallerMethodIsPublic());
    contexts.add(new CallerMethodIsStatic());
    // contexts.add(new CallerMethodReturnsNull());
    contexts.add(new VariableIsObjectType());
    contexts.add(new VariableIsFinal());
    // contexts.add(new InvocationIsIsolated());
    // contexts.add(new InvocationIsBase());
    contexts.add(new InvocationIsConstructorArgument());

    contexts.addAll(NameContext.all);

    calleeContexts.add(new CalleeMethodReturnsVoid());
    calleeContexts.add(new CalleeMethodReturnsLiteral());
    calleeContexts.add(new CalleeMethodThrows());
    calleeContexts.add(new CalleeMethodChecksNull());
    // calleeContexts.add(new CalleeMethodChecksNullForNullParameter());
    calleeContexts.add(new CalleeMethodReturnsNew());
    calleeContexts.add(new CalleeMethodUsedAsBase());
    calleeContexts.add(new CalleeMethodReturnsField());
  }

  public static List<Context> getAllContexts() {
    return contexts;
  }

  public static List<Context> getCalleeContexts() {
    return calleeContexts;
  }
}