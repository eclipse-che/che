package p;
class A extends Exception{
}
class X{
	void m(){
		class B extends Exception{}
		class C{
			C() throws A{
			}
		}
	}
}