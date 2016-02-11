package p;
//can't rename m to k
class C {
	public void k(){}
}
class B extends C{
}
class A extends B implements I{
	public void m(){}
}
interface I{
	public void m();
}
