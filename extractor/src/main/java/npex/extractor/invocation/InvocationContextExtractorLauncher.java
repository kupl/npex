package npex.extractor.invocation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import npex.common.NPEXException;
import npex.common.NPEXLauncher;

public class InvocationContextExtractorLauncher extends NPEXLauncher {
  private final HashSet<String> traceMethodNames = new HashSet<String>();

  public InvocationContextExtractorLauncher(File projectRoot, boolean loadFromCache, String[] classpath,
      String resultsPath, File trace) throws IOException {
    super(projectRoot, loadFromCache, classpath);
    if (trace != null) {
      String jsonContents = new String(Files.readAllBytes(Paths.get(trace.getPath())));
      for (var proc : (new JSONObject(jsonContents)).getJSONArray("procs")) {
        traceMethodNames.add(((JSONObject) proc).getString("class"));
      }
    }

    spoonLauncher.addProcessor(new InvocationContextProcessor(resultsPath, traceMethodNames));
  }

  public void run() throws NPEXException {
    spoonLauncher.process();
  }
}