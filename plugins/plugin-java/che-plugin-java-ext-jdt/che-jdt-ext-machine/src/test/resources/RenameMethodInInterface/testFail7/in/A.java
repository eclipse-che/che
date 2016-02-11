package p;
//can't rename I.m to k
abstract class B{
	abstract void k();
}
class A extends B implements I{
	public void m(){}
}
interface I{
	public void m();
}