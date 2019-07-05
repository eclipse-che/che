package p;
class A{
	/**
	 * @see #f()
	 * @see #f()
	 * @see A#f()
	 */
	static void f() {
		f();
		f();
		new A().f();
		A. f ();
		new A(). f/*unicode*/();
	}
}
