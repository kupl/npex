package npex.extractor;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import npex.common.NPEInfo;
import npex.common.NPEXException;
import npex.common.NPEXLauncher;
import spoon.reflect.code.CtExpression;

public class NullInvocationExtractorLauncher extends NPEXLauncher {
  private NPEInfo npeInfo;

  public NullInvocationExtractorLauncher(File projectRoot, boolean loadFromCache, String[] classpath, File npeReport)
      throws IOException, NoSuchElementException {
    super(projectRoot, loadFromCache, classpath);
    this.npeInfo = NPEInfo.readFromJSON(factory.getModel(), npeReport.getAbsolutePath());
  }

  public void run() throws NPEXException {
    CtExpression npe = npeInfo.resolve();
  }
}