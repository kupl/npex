package npex;

import java.util.List;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import npex.buggycode.BuggyCode;
import npex.buggycode.NullHandleTernary;
import npex.template.PatchTemplateDeveloper;
import spoon.reflect.code.CtExpression;

public class BuggyCodeTest {
  protected Logger logger = Logger.getLogger(BuggyCodeTest.class);

  protected List<BuggyCode> buggyCodes;

  @Before
  public void setup() {
    MavenPatchExtractor extractor = new MavenPatchExtractor(
        "/media/4tb/npex/npex_data/benchmarks/sling-org-apache-sling-tracer/");
    this.buggyCodes = extractor.extractBuggyCodes();
  }

  protected void testWithBuggy(Consumer<BuggyCode> consumer) {
    buggyCodes.forEach(consumer);
  }

  private Consumer<BuggyCode> generateBuggyBlockConsumer = buggy -> {
    CtExpression<?> nullPointer = buggy.getNullPointer();
    logger.info("Null Handle Type: " + buggy.getNullHandle().getClass());
    logger.info("Null Pointer: " + nullPointer);
    logger.info("Original Block: ");
    logger.info(buggy.getOriginalBlock());
    logger.info("Buggy Block: ");
    logger.info(buggy.getBuggyBlock());
  };

  @Test
  public void testBuggyCodes() {
    testWithBuggy(generateBuggyBlockConsumer);
  };

  @Test
  public void testDeveloperPatches() {
    testWithBuggy(generateBuggyBlockConsumer.andThen(buggy -> {
      PatchTemplateDeveloper template = buggy.generateDeveloperPatch();
      template.apply();
      logger.info("Patched Block");
      logger.info(template.getBlock());
    }));
  }

  @Test
  public void testPrintNullExp() {
    testWithBuggy(buggy -> {
      CtExpression<?> nullPointer = buggy.getNullPointer();
      logger.info(buggy.getID());
      logger.info(nullPointer);
      logger.info(nullPointer.getParent(CtExpression.class));
      logger.info(buggy.getOriginalBlock());
      logger.info(buggy.getBuggyBlock());
    });

  }

  @Test
  public void testBuggyCodesFromTernaries() {
    this.buggyCodes.stream().filter(buggy -> (buggy.getNullHandle() instanceof NullHandleTernary))
        .forEach(generateBuggyBlockConsumer);
  }
}
