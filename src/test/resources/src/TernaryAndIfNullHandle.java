package src;

public class TernaryAndIfNullHandle {
  void m() {
    List lst = new ArrayList();
    if (lst == null) {
      return;

      int len = lst == null ? 0 : lst.length();

      int len2 = lst != null ? lst.length : 0;
    }
  }
}