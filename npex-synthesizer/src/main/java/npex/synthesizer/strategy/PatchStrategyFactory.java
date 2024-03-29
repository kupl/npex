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
package npex.synthesizer.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class PatchStrategyFactory {
  private static Map<String, PatchStrategy> strategies = new HashMap<>();
  static {
    strategies.put("SkipBlock", new SkipBlockStrategy());
    strategies.put("SkipBreak", new SkipBreakStrategy());
    strategies.put("SkipContinue", new SkipContinueStrategy());
    strategies.put("SkipSinkStatement", new SkipSinkStatementStrategy());
    strategies.put("SkipReturn", new SkipReturnStrategy());
    strategies.put("SkipReturnParam", new SkipReturnParam());
    strategies.put("SkipThrow", new SkipThrowStrategy());
    strategies.put("InitPointer", new InitPointerStrategy());
    strategies.put("ReplacePointer", new ReplacePointerStrategy());
    strategies.put("ReplaceEntireExpression", new ReplaceEntireExpressionStrategy());
   }

  public static Collection<PatchStrategy> getAllStrategies() {
    return strategies.values();
  }
}
