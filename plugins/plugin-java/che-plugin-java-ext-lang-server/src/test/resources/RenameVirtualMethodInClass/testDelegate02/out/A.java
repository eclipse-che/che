package p;

public abstract class A {
	/**
	 * @deprecated Use {@link #k()} instead
	 */
	abstract void m();

	abstract void k();
}

class B extends A {
	/**
	 * @deprecated Use {@link #k()} instead
	 */
	void m() {
		k();
	}

	void k() {
		//Foo
	}
}

class C extends B {
	/**
	 * @deprecated Use {@link #k()} instead
	 */
	void m() {
		k();
	}

	void k() {
		//Bar
	}
}
