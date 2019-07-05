package p;
//can't rename A.m - declared in superclass
class B {
	public native void m();
}
class A extends B implements I{
	public void m(){}
}
interface I{
	void m();
}
