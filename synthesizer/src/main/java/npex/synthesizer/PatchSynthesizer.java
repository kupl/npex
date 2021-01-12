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

import java.util.ArrayList;
import java.util.List;

import npex.synthesizer.buggycode.BuggyCode;
import npex.synthesizer.buggycode.NullHandle;
import npex.synthesizer.buggycode.NullHandleIf;
import npex.synthesizer.buggycode.NullHandleTernary;
import npex.synthesizer.strategy.PatchStrategy;
import npex.synthesizer.template.PatchTemplate;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

public class PatchSynthesizer extends AbstractDriver {
  private final List<PatchStrategy> strategies;

  public PatchSynthesizer(final String projectRootPath) {
    this(projectRootPath, new ArrayList<>());
  }

  public PatchSynthesizer(final String projectRootPath, List<PatchStrategy> strategies) {
    super(projectRootPath);
    this.strategies = strategies;
  }

  protected void setupLauncher() {
  }

  public ArrayList<NullHandle> extractNullHandles() {
    ArrayList<NullHandle> handles = new ArrayList<>();
    for (CtElement el : launcher.getFactory().getModel().getElements(new TypeFilter<>(CtCodeElement.class))) {
      try {
        if (el instanceof CtIf) {
          handles.add(new NullHandleIf((CtIf) el));
        } else if (el instanceof CtConditional)
          handles.add(new NullHandleTernary((CtConditional<?>) el));
      } catch (IllegalArgumentException e) {
        continue;
      }
    }
    return handles;
  }

  public List<BuggyCode> extractBuggyCodes() {
    List<BuggyCode> buggyCodes = new ArrayList<>();
    for (NullHandle handle : this.extractNullHandles()) {
      BuggyCode bug = new BuggyCode(projectRoot.getAbsolutePath(), handle);
      try {
        bug.stripNullHandle();
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (bug.hasNullPointerIdentifiable() && bug.isAccessPathResolved() && !bug.isBugInConstructor())
        buggyCodes.add(bug);
    }
    return buggyCodes;
  }

  public spoon.reflect.factory.Factory getFactory() {
    return launcher.getFactory();
  }

  public List<PatchTemplate> generatePatchTemplates(BuggyCode buggy) {
    List<PatchTemplate> templates = new ArrayList<PatchTemplate>();
    CtExpression<?> nullExpr = buggy.getNullPointer();
    // templates.add(buggy.generateDeveloperPatch());
    strategies.stream().filter(s -> s.isApplicable(nullExpr)).forEach(s -> templates.addAll(s.enumerate(nullExpr)));
    return templates;
  }
}