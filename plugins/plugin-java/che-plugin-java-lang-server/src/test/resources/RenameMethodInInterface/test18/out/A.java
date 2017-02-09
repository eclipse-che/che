//renaming I.m to k
package p;
interface I {
	void k();
}
interface J{
	void k();
}
interface J2 extends J{
	void k();
}

class A{
	public void k(){}
}
class C extends A implements I, J{
	public void k(){}
}