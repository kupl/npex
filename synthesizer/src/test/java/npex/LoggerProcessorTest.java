package npex.synthesizer;

import org.junit.Before;
import org.junit.Test;

import npex.synthesizer.errortracer.ErrorTracerDriver;

public class LoggerProcessorTest {
  ErrorTracerDriver driver;

  @Before
  public void setup() {
    String projectRootPath = "/media/4tb/npex/npex_data/benchmarks-bears/Bears-189-buggy";
    this.driver = new ErrorTracerDriver(projectRootPath);
  }

  @Test
  public void test() {
    driver.run();
  }
}