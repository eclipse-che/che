package p;
class A{
static void m(){}
}
class B extends A{
static void m(){};
}
class Test{
void f(){
	new A().m();
	A.m();
	new B().m();
	B.m();
}
}