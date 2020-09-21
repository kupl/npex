package npex;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import npex.strategy.ObjectInitializer;
import npex.strategy.ReplaceNullLiteralStrategy;
import npex.strategy.VarInitializer;
import npex.template.PatchTemplate;
import spoon.Launcher;
import spoon.reflect.code.CtExpression;
import spoon.reflect.visitor.filter.TypeFilter;

public class SingleSourceTest {
  private Launcher launcher;
  Logger logger = Logger.getLogger(SingleSourceTest.class);

  @Before
  public void setup() {
    this.launcher = new Launcher();
    launcher.addInputResource("./src/test/resources/src/Main.java");
    launcher.buildModel();
  }

  @Test
  public void testNullLitType() {
    List<CtExpression<?>> exprs = launcher.getModel().getElements(new TypeFilter(CtExpression.class));
    for (CtExpression<?> exp : exprs) {
      System.out.println(String.format("Type of %s: %s, %s", exp, exp.getType().toString(), exp.getType().box()));
    }
  }

  @Test
  public void testNullSourceAsSink() {
    try {
      NPEInfo npe = NPEInfo.readFromJSON(launcher.getFactory(), "./src/test/resources/src/npe_null_source.json");
      CtExpression<?> null_exp = npe.resolve();
      assertEquals(null_exp.toString(), "null");
      List<PatchTemplate> templates = (new ReplaceNullLiteralStrategy(new VarInitializer())).generate(null_exp);
      List<PatchTemplate> templates2 = (new ReplaceNullLiteralStrategy(new ObjectInitializer())).generate(null_exp);
      templates2.addAll(templates);
      for (PatchTemplate template : templates2) {
        logger.info(template.apply());
      }
    } catch (IOException e) {
      logger.fatal(e.getMessage());
    }

  }

}