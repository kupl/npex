package npex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import npex.buggycode.BuggyCode;
import npex.buggycode.NullHandle;
import npex.buggycode.NullHandleIf;
import npex.buggycode.NullHandleTernary;
import spoon.MavenLauncher;
import spoon.MavenLauncher.SOURCE_TYPE;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtIf;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.filter.TypeFilter;

public class MavenPatchExtractor {
  final MavenLauncher launcher;
  private final String projectRootPath;
  private final String projectName;

  public MavenPatchExtractor(final String mavenProjectPath) throws Exception {
    launcher = new MavenLauncher(mavenProjectPath, SOURCE_TYPE.APP_SOURCE);
    launcher.getEnvironment().setAutoImports(true);

    launcher.run();
    this.projectRootPath = FilenameUtils.getFullPathNoEndSeparator(mavenProjectPath);
    this.projectName = FilenameUtils.getBaseName(projectRootPath);
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
    return this.extractNullHandles().stream().map(x -> new BuggyCode(this.projectName, x))
        .peek(x -> x.stripNullHandle()).collect(Collectors.toList());
  }

  public spoon.reflect.factory.Factory getFactory() {
    return launcher.getFactory();
  }
}