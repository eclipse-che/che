package p;
class A{
	int g;
}
class B extends A{
	A a;
	void m(){
		int g= a.g;
	}
}