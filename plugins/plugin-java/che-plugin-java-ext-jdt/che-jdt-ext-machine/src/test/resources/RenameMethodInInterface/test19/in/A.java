//renaming I.m to k
package p;
interface I {
	void m();
}
interface J{
	void m();
}
interface J2 extends J{
	void m();
}

class A{
	private void m(){}
}
class C extends A implements I, J{
	public void m(){}
}
