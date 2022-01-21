package npex.common;

import java.io.File;
import java.io.IOException;

public class DefaultNPEXLauncher extends NPEXLauncher {
  public DefaultNPEXLauncher(File projectRoot, boolean loadFromCache, String[] classpath) throws IOException {
    super(projectRoot, loadFromCache, classpath);
  }

  /* Do nothing */
  public void run() throws NPEXException {
  }

}