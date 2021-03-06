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
package npex.synthesizer;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spoon.Launcher;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.visitor.filter.TypeFilter;

public class SingleSourceTest {
  private Launcher launcher;
  Logger logger = LoggerFactory.getLogger(SingleSourceTest.class);

  @Before
  public void setup() {
    this.launcher = new Launcher();
    launcher.addInputResource("./src/test/resources/src/single_source/Main.java");
    launcher.buildModel();
  }

  @Test
  public void testNullLitType() {
    List<CtExpression<?>> exprs = launcher.getModel().getElements(new TypeFilter(CtExpression.class));
    for (CtExpression<?> exp : exprs) {
      System.out.println(String.format("Type of %s: %s, %s", exp, exp.getType().toString(), exp.getType().box()));
    }
  }

  @Test
  public void testTargetedExpr() {
    List<CtFieldAccess<?>> exprs = launcher.getModel().getElements(new TypeFilter(CtFieldAccess.class));
    for (CtFieldAccess<?> exp : exprs) {
      System.out.println(String.format("Fieldaccess: %s, var: %s, target: %s, parent: %s, parent: %s", exp,
          exp.getVariable(), exp.getTarget(), exp.getParent(), exp.getParent().getParent()));
    }
  }

  @Test
  public void testResolveNPE() {
    try {
      NPEInfo npeInfo = NPEInfo.readFromJSON(launcher.getModel(), "./src/test/resources/src/single_source/npe.json");
      CtExpression npe = npeInfo.resolve();
      System.out.println(npe + ", " + npe.getClass());
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }

}