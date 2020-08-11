package Java_exn;

class A {
  A g() {
    return new A();
  }

  int h() {
    return 0;
  }
}

class B {
  A f;

  B() {
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

class Java_exn1 {
  public static void main(String args[]) {
    B b = new B();
    b.init(false);
    b.f.g().h();
    b.read();
  }
}
