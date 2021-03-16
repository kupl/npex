package npex.extractor;

import java.io.File;
import java.io.IOException;

import npex.common.NPEXException;
import npex.common.NPEXLauncher;
import npex.extractor.processors.InvoContextProcessor;

public class InvoContextExtractorLauncher extends NPEXLauncher {
  public InvoContextExtractorLauncher(File projectRoot, boolean loadFromCache, String[] classpath, String resultsPath)
      throws IOException {
    super(projectRoot, loadFromCache, classpath);
    spoonLauncher.addProcessor(new InvoContextProcessor(resultsPath));
  }

  public void run() throws NPEXException {
    spoonLauncher.process();
  }
}