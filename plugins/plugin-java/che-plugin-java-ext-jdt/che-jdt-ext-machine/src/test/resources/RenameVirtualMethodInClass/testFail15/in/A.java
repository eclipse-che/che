package p;
//can't rename m to k
class C {
	void k(){}
}
class B extends C{
}
class A extends B{
	void m(){}
}