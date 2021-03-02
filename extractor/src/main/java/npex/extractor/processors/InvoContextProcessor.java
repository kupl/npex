package npex.extractor.processors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import npex.extractor.context.ContextExtractor;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;

public class InvoContextProcessor extends AbstractProcessor<CtInvocation> {
  static Logger logger = LoggerFactory.getLogger(InvoContextProcessor.class);
  final private File resultsOut;

  final private Map<InvocationKey, Map<String, Boolean>> invoContexts = new HashMap<>();

  public InvoContextProcessor(String resultsPath) {
    super();
    this.resultsOut = new File(resultsPath);
  }

  @Override
  public boolean isToBeProcessed(CtInvocation candidate) {
    return candidate.getPosition().isValidPosition();
  }

  @Override
  public void process(CtInvocation invo) {
    if (!invo.getExecutable().isStatic()) {
      invoContexts.put(new InvocationKey(invo, -1), ContextExtractor.extract(invo, -1));
    }

    for (int nullPos = 0; nullPos < invo.getArguments().size(); nullPos++) {
      invoContexts.put(new InvocationKey(invo, nullPos), ContextExtractor.extract(invo, nullPos));
    }
  }

  @Override
  public void processingDone() {
    JSONArray jsonArray = new JSONArray();
    for (InvocationKey key : invoContexts.keySet()) {
      JSONObject js = new JSONObject();
      js.put("sourcePath", key.sourcePath);
      js.put("line", key.line);
      js.put("methodName", key.methodName);
      js.put("nullPos", key.nullPos);
      js.put("contexts", new JSONObject(invoContexts.get(key)));
      jsonArray.put(js);

      if (key.sourcePath.equals(
          "/media/4tb/npex/NPEX_DATA/caelum-stella-e73113f-buggy/stella-faces/src/main/java/br/com/caelum/stella/faces/validation/StellaCPFValidator.java")) {
        System.out.println("KeyFound!");
        System.out.println(key.methodName);
        System.out.println(key.line);
        System.out.println(js);
      }
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultsOut))) {
      jsonArray.write(writer, 1, 4);
    } catch (IOException e) {
    }
    return;
  }
}

class InvocationKey {
  final String sourcePath;
  final int line;
  final String methodName;
  final int nullPos;

  public InvocationKey(CtInvocation invo, int nullPos) {
    this.sourcePath = invo.getPosition().getFile().toString();
    this.line = invo.getPosition().getLine();
    this.methodName = invo.getExecutable().getSimpleName();
    this.nullPos = nullPos;
  }
}