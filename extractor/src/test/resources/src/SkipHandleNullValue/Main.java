public class Main {
  String p = getClass().getName();
  String q = getClass().getName();

  public void nullModelShouldReturnsDefaultValue() {
    int x;

    if (p != null) {
      x = p.length();
    }
  }

  public void nullValueIsUnknown() {
    int x = 2;

    if (q.isBlank()) {
      x = 1;
    }

    if (p != null) {
      x = p.length();
    }
  }

  public void nullValueInsideLoop() {
    int x = 3;

    while (q.isBlank()) {
      x = q.length();

      if (p != null)
        x = java.lang.Integer(p);
    }
  }

  public void nullValueOutsideOfMyBlock() {
    int x = 5;

    while (q.isBlank()) {
      return;
    }

    x = 10;
    if (q.isEmpty()) {
      if (p != null) {
        x = p.codePointAt(x);
      }
    }

    x = 3;
  }

  public void exceptionExprTest() {
    if (p == null) {
      throw new IllegalArgumentException();
    }

    String s = p == null ? null : new String(p);

    Exception e = new NullPointerException();
  }

  public String getFontFamily() {
    String retval = null;
    COSString name = (new Double(1.0)).toString();
    if (name != null) {
      retval = name.getString();
    }
    return retval;
  }

  class COSString {
    public String getString() {
      return "aaa";
    }
  }
}