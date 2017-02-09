package p;
//renaming I.m would require renaming a native method
class A implements I{
	public void m(){}
}
class B extends A{
	public native void m();
}
interface I{
	void m();
}