package p;
//can't rename I.m to k
class B{
	public void k(int x){}
}
class A extends B implements I{
	public void m(int f){}
}
interface I{
	public void m(int y);
}