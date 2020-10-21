package npex.synthesizer;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import npex.synthesizer.buggycode.NullHandle;

public class NullHandleTest {
  protected List<NullHandle> nullHandles;
  protected List<NullHandle> ternaries;

  @Before
  public void setup() throws Exception {
    PatchSynthesizer synthesizer = new PatchSynthesizer("/media/4tb/npex/npex_data/benchmarks/maven-release/");
    this.ternaries = synthesizer.extractNullHandles();
  }

  @Test
  public void test() {
    this.ternaries.forEach(x -> {
      System.out.println(x.getStatement().getParent());
    });
  }
}