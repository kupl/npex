package npex.errortracer;

import java.util.ArrayList;
import java.util.List;

public class Trace {
  public final static List<String> trace = new ArrayList<>();

  public static void add(String str) {
    trace.add(str);
  }

  public static void print() {
    System.out.println(trace);
  }
}