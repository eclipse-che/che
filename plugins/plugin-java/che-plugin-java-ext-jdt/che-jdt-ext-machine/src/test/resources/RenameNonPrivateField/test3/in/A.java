package p;
/**
 * @see #f
 * @see A#f
 * @see p.A#f
 * @see B#f
 */
class A{
	protected int f;
	void m(){
		f++;
	}
}
/**
 * @see #f
 */
class B{
	A a;
	protected int f;
	void m(){
		a.f= 0;
	}
}