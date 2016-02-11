package p;
//can't rename m in A  - must do it in B
class B{
	void m(){}
}
class A extends B{
	void m(){}
}