package p;
/**
 * @see #g
 * @see A#g
 * @see p.A#g
 * @see B#f
 */
class A{
	protected int g;
	void m(){
		g++;
	}
}
/**
 * @see #f
 */
class B{
	A a;
	protected int f;
	void m(){
		a.g= 0;
	}
}