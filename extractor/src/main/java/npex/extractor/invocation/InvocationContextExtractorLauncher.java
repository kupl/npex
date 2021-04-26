package npex.extractor.invocation;

import java.io.File;
import java.io.IOException;

import npex.common.NPEXException;
import npex.common.NPEXLauncher;

public class InvocationContextExtractorLauncher extends NPEXLauncher {
  public InvocationContextExtractorLauncher(File projectRoot, boolean loadFromCache, String[] classpath,
      String resultsPath) throws IOException {
    super(projectRoot, loadFromCache, classpath);
    spoonLauncher.addProcessor(new InvocationContextProcessor(resultsPath));
  }

  public void run() throws NPEXException {
    spoonLauncher.process();
  }
}