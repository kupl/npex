package npex.extractor.invocation;

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

import npex.common.NPEXException;
import npex.extractor.context.ContextExtractor;
import npex.extractor.invocation.InvocationContextProcessor.InvocationKeyMap;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtInvocation;

public class InvocationContextProcessor extends AbstractProcessor<CtInvocation> {
  static Logger logger = LoggerFactory.getLogger(InvocationContextProcessor.class);
  final private File resultsOut;

  final private Map<InvocationSite, InvocationKeyMap> invoContextsMap = new HashMap<>();

  public InvocationContextProcessor(String resultsPath) {
    super();
    this.resultsOut = new File(resultsPath);
  }

  @Override
  public boolean isToBeProcessed(CtInvocation candidate) {
    return candidate.getPosition().isValidPosition();
  }

  @Override
  public void process(CtInvocation invo) {
    var site = new InvocationSite(invo);
    var keyMap = new InvocationKeyMap();
    for (int nullPos = invo.getExecutable().isStatic() ? 0 : -1; nullPos < invo.getArguments().size(); nullPos++) {
      try {
        keyMap.put(new InvocationKey(invo, nullPos), ContextExtractor.extract(invo, nullPos));
        invoContextsMap.put(site, keyMap);
      } catch (NPEXException e) {
        logger.error(e.getMessage());
        continue;
      }
    }
  }

  @Override
  public void processingDone() {
    JSONArray sites = new JSONArray();
    for (var entry : invoContextsMap.entrySet()) {
      JSONObject site = new JSONObject();
      sites.put(site);
      site.put("site", entry.getKey().toJSON());
      site.put("keycons", entry.getValue().toJSONArray());
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(resultsOut))) {
      writer.write(sites.toString(4));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return;
  }

  class InvocationKeyMap extends HashMap<InvocationKey, Map<String, Boolean>> {
    public JSONArray toJSONArray() {
      JSONArray array = new JSONArray();
      for (var entry : this.entrySet()) {
        JSONObject entryObject = new JSONObject();
        entryObject.put("key", entry.getKey().toJSON());
        entryObject.put("contexts", entry.getValue());
        array.put(entryObject);
      }
      return array;
    }

  }
}
