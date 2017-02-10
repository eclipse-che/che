package p;
class A{
static void k(){}
}
class B extends A{

}
class Test{
void f(){
	new A().k();
	A.k();
	new B().k();
	B.k();
}
}