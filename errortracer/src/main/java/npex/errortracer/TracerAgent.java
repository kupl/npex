package npex.errortracer;

import java.lang.instrument.Instrumentation;

public class TracerAgent {
  public static void premain(String agentArgs, Instrumentation inst) {
    inst.addTransformer(new TracerTransformer());
  }
}