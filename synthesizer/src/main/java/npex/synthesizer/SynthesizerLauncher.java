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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import npex.common.NPEXException;
import npex.common.NPEXLauncher;
import npex.synthesizer.strategy.PatchStrategy;
import npex.synthesizer.strategy.PatchStrategyFactory;
import npex.synthesizer.template.PatchTemplate;
import spoon.reflect.code.CtExpression;

@SuppressWarnings("rawtypes")
public class SynthesizerLauncher extends NPEXLauncher {
  private static Collection<PatchStrategy> strategies = PatchStrategyFactory.getAllStrategies();
  private File npeReport;

  public SynthesizerLauncher(File projectRoot, boolean loadFromCache, File npeReport) throws IOException {
    super(projectRoot, loadFromCache);
    this.npeReport = npeReport;
  }

  private List<PatchTemplate> eneumeratePatches(CtExpression nullExp) {
    List<PatchTemplate> templates = new ArrayList<>();
    for (PatchStrategy stgy : strategies) {
      if (stgy.isApplicable(nullExp)) {
        List<PatchTemplate> generated = stgy.enumerate(nullExp);
        logger.info("Strategy {} is applicable! (total {} templates are generated): ", stgy.getName(),
            generated.size());
        templates.addAll(generated);
      } else {
        logger.info("Strategy {} is not applicable", stgy.getName());
      }
    }
    return templates;
  }

  public void run() throws NPEXException {
    File patchesDir = new File(projectRoot, "patches");
    patchesDir.mkdirs();
    try {
      NPEInfo npeInfo = NPEInfo.readFromJSON(factory.getModel(), npeReport.getAbsolutePath());
      CtExpression nullExp = npeInfo.resolve();
      for (PatchTemplate patch : eneumeratePatches(nullExp)) {
        logger.info("Patch ID: {}", patch.getID());
        logger.info("-- Original statement: {}", patch.getOriginalStatement());
        patch.apply();
        logger.info("-- Patched statement: {}", patch.getPatchedStatement());
        File patchDir = new File(patchesDir, patch.getID());
        patch.store(projectRoot.getAbsolutePath(), patchDir);
      }
    } catch (IOException | NoSuchElementException e) {
      e.printStackTrace();
      throw new NPEXException(e.getMessage());
    }
  }

}
