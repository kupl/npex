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

import org.junit.Test;

import spoon.Launcher;
import spoon.processing.ProcessingManager;
import spoon.reflect.factory.Factory;
import spoon.support.QueueProcessingManager;

public class NullHandleProcessorTest {
  protected Launcher launcher;

  @Test
  public void testCatchProcessor() throws Exception {
    final String[] args = { "-i", "/home/june/project/npex/extractor/src/test/resources/src/Java_exn1.java", "-o",
        "target/spooned/" };

    final Launcher launcher = new Launcher();
    launcher.setArgs(args);
    launcher.run();

    final Factory factory = launcher.getFactory();
    final ProcessingManager processingManager = new QueueProcessingManager(factory);
    final NullHandleProcessor processor = new NullHandleProcessor();
    processingManager.addProcessor(processor);
    processingManager.process(factory.Class().getAll());
  }

}