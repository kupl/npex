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

import npex.extractor.InvocationKey;
import npex.extractor.context.ContextExtractor;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;

public class InvoContextProcessor extends AbstractProcessor<CtInvocation> {
  static Logger logger = LoggerFactory.getLogger(InvoContextProcessor.class);
  final private File resultsOut;

  final private Map<InvocationKeyWithLoc, Map<String, Boolean>> invoContexts = new HashMap<>();

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
    String source_path = invo.getPosition().getFile().getAbsolutePath();
    int line = invo.getPosition().getLine();

    for (int nullPos = invo.getExecutable().isStatic() ? 0 : -1; nullPos < invo.getArguments().size(); nullPos++) {
      var key = new InvocationKeyWithLoc(source_path, line, invo, nullPos);
      invoContexts.put(key, ContextExtractor.extract(invo, nullPos));
    }
  }

  @Override
  public void processingDone() {
    JSONArray jsonArray = new JSONArray();
    for (var key : invoContexts.keySet()) {
      JSONObject js = key.toJSON();
      js.put("contexts", new JSONObject(invoContexts.get(key)));
      jsonArray.put(js);
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultsOut))) {
      jsonArray.write(writer, 1, 4);
    } catch (IOException e) {
    }
    return;
  }
}

class InvocationKeyWithLoc extends InvocationKey {
  public final String sourcePath;
  public final int lineno;

  public InvocationKeyWithLoc(String sourcePath, int lineno, CtInvocation invo, int nullPos) {
    super(invo, nullPos);
    this.sourcePath = sourcePath;
    this.lineno = lineno;
  }

  public JSONObject toJSON() {
    var obj = super.toJSON();
    obj.put("source_path", sourcePath);
    obj.put("lineno", lineno);
    return obj;
  }

}
