package p;
//can't rename I.m to k - defined in subclass
class A implements I{
	public void m(){}
}
class B extends A{
	public private void k(){}
}
interface I{
	void m();
}