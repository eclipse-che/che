package p;
class A{
	/**
	 * @see #e()
	 * @see #\u0065()
	 * @see A#\u0065()
	 */
	static void \u0065() {
		\u0065();
		e();
		new A().\u0065();
		A. e ();
		new A(). \u0065/*unicode*/();
	}
}
