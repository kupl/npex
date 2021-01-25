package src;

import src.p3.C;

import java.util.Vector;

public class Main {
	private class A {
		int af;
	}

	private class B {
		A a = new A();
		int f;

		A getA() {
			return a;
		}
	}

	B field_for_class_b;

	public static void main(String[] args) {
		try {
			B b = null;
			m1();
		} catch (Exception ignored) {
		}
	}

	public static void m1() throws Exception {
		B b = new B();
		b.f = 1;
		m2();
	}

	public static void m2() throws Exception {
		B b = new B();
		b.a.af = 2;
		b.getA().af = 3;
		throw new RuntimeException();
	}

	public void m(C c) throws Exception {
	}

	public void loop(String[] args) {
		for (String arg : args) {
			System.err.println(arg);
			System.err.println(arg);
		}

		while (true) {
			System.out.println(args.toString());
		}
	}

	public void m2(String[] args) {
		Vector<?> v = null;
		String str = null;
		if (args == null) {
			;
		}
	}
}
