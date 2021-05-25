package npex.extractor.invocation;

import spoon.reflect.visitor.filter.TypeFilter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

import npex.common.NPEXException;
import npex.common.NPEXLauncher;
import spoon.reflect.CtModel;
import spoon.reflect.CtModelImpl;
import spoon.reflect.declaration.CtClass;

public class InvocationContextExtractorLauncher extends NPEXLauncher {
  private final HashMap<String, CtClass> classNameMap = new HashMap<>();
  private final HashSet<String> traceClassNames = new HashSet<>();

  public InvocationContextExtractorLauncher(File projectRoot, boolean loadFromCache, String[] classpath,
      String resultsPath, File trace) throws IOException {
    super(projectRoot, loadFromCache, classpath);
    if (trace != null) {
      initClassNameMap(factory.getModel());
      String jsonContents = new String(Files.readAllBytes(Paths.get(trace.getPath())));
      for (var proc : (new JSONObject(jsonContents)).getJSONArray("procs")) {
        String className = ((JSONObject) proc).getString("class");
        if (classNameMap.containsKey(className)) {
          traceClassNames.add(classNameMap.get(className).getQualifiedName());
        }
      }
    }
    spoonLauncher.addProcessor(new InvocationContextProcessor(resultsPath, traceClassNames));
  }

  public void run() throws NPEXException {
    spoonLauncher.process();
  }

  private void initClassNameMap(CtModel model) {
    for (CtClass clazz : model.getElements(new TypeFilter<>(CtClass.class))) {
      classNameMap.put(clazz.getQualifiedName(), clazz);
    }
  }

}