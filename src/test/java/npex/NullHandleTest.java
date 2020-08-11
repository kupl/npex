package npex;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import npex.buggycode.NullHandle;

public class NullHandleTest {
  protected List<NullHandle> nullHandles;
  protected List<NullHandle> ternaries;

  @Before
  public void setup() throws Exception {
    MavenPatchExtractor extractor = new MavenPatchExtractor("/media/4tb/npex/npex_data/benchmarks/maven-release/");
    this.ternaries = extractor.extractNullHandles();
  }

  @Test
  public void test() {
    this.ternaries.forEach(x -> {
      System.out.println(x.getStatement().getParent());
    });
  }
}