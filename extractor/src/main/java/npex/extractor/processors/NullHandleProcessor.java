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
package npex.extractor.processors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.extractor.nullhandle.AbstractNullHandle;
import npex.extractor.nullhandle.NullHandleFactory;
import npex.extractor.nullhandle.NullModel;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeElement;

public class NullHandleProcessor extends AbstractProcessor<CtCodeElement> {
  static Logger logger = LoggerFactory.getLogger(NullHandleProcessor.class);
  public List<AbstractNullHandle> handles = new ArrayList<>();

  @Override
  public void process(CtCodeElement element) {
    logger.info("Processing element whose type is {}", element.getClass().getSimpleName());
    AbstractNullHandle handle = NullHandleFactory.createNullHandle(element);
    if (handle != null) {
      logger.info("-- Extracting succeeds: {}", element);
      List<NullModel> models = handle.collectNullModels();
      if (models.isEmpty()) {
        logger.info("-- No models collected for the handle");
        return;
      }
      for (NullModel model : models) {
        logger.info("-- Null invocation of model: {}", model.getNullInvocation());
      }
      handles.add(handle);
    }
  }

  @Override
  public void processingDone() {
    return;
  }
}
