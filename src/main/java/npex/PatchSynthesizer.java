package npex;

import java.util.ArrayList;
import java.util.List;

import npex.buggycode.BuggyCode;
import npex.buggycode.NullHandle;
import npex.buggycode.NullHandleIf;
import npex.buggycode.NullHandleTernary;
import npex.strategy.PatchStrategy;
import npex.template.PatchTemplate;
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
        if (el instanceof CtIf)
          handles.add(new NullHandleIf((CtIf) el));
        else if (el instanceof CtConditional)
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
      try {
        BuggyCode bug = new BuggyCode(projectRoot.getAbsolutePath(), handle);
        if (bug.hasNullPointerIdentifiable() && bug.isAccessPathResolved() && !bug.isBugInConstructor()
            && bug.stripNullHandle() != null) {
          buggyCodes.add(bug);
        }
      } catch (Exception e) {
        continue;
      }
    }
    return buggyCodes;
  }

  public spoon.reflect.factory.Factory getFactory() {
    return launcher.getFactory();
  }

  public List<PatchTemplate> generatePatchTemplates(BuggyCode buggy) {
    List<PatchTemplate> templates = new ArrayList<PatchTemplate>();
    CtExpression<?> nullExpr = buggy.getNullPointer();
    templates.add(buggy.generateDeveloperPatch());
    strategies.stream().filter(s -> s.isApplicable(nullExpr)).forEach(s -> templates.addAll(s.generate(nullExpr)));
    return templates;
  }
}