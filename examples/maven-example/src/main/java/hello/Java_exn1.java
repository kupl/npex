package hello;
import org.junit.Assert;

public class Java_exn1 {
  public static String m(Object obj, String valueIfNull) {
    // if (obj == null) return valueIfNull; // developer's patch
    Class cls = obj.getClass(); // NPE
    String name = cls.getCanonicalName();
    if (name == null) {
      return valueIfNull;
    } else {
      return name;
    }
  }
  
  public static void goo() {
    m(null, "null");
  }
}
