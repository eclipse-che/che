package p;
//can't rename A.m - declared in superclass
class B {
	native void m();
}
class A extends B{
	void m(){}
}
