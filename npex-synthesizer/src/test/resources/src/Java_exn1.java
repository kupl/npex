
class A {
  A g() {
    return new A();
  }

  int h() {
    return 0;
  }

  static int getInt() {
    return 0;
  }
}

class B {
  A f;

  B(int i) {
    f = null;
  }

  void init(boolean b) {
    if (b)
      f = new A();
  }

  void read() {
    if (f == null)
      throw new IllegalArgumentException();
    if (true)
      System.out.println("Good-buy");
  }

}

class C extends B{
  C() {
    super(A.getInt());
    System.out.println("After super");
}
}


class Java_exn1 {
  public static void main(String args[]) {
    C c = new C();
    B b = new B(A.getInt()); 
    b.init(false);
    b.f.g().h();
    b.read();
  }
}
