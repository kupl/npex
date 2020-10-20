package npex.synthesizer.instrument;

import java.lang.instrument.Instrumentation;

public class DurationAgent {
  public static void premain(String agentArgs, Instrumentation inst) {
    System.out.println("Executing premain.........");
    inst.addTransformer(new DurationTransformer());
  }
}