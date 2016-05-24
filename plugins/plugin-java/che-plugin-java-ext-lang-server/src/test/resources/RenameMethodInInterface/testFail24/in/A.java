package p;
//can't rename I.m to k
class B{
	public void k(){}
}
class A extends B implements I{
	public void m(){}
}
interface I{
	abstract void m();
}