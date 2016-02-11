package p;
class A{
static void k(){}
}
class B extends A{
static void m(){};
}
class Test{
void f(){
	new A().k();
	A.k();
	new B().m();
	B.m();
}
}