package p;

public class A {
	/**
	 * @deprecated Use {@link #k()} instead
	 */
	void m() {
		k();
	}

	void k() { }
}
