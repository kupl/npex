package src;

import src.p3.C;

import java.util.Vector;

public class Main {
	public static void main(String[] args) {
		try {
			@SuppressWarnings("unused")
			Vector<?> v = null;
			m1();
		} catch (Exception ignored) {
		}
	}

	public static void m1() throws Exception {
		m2();
	}

	public static void m2() throws Exception {
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
}
