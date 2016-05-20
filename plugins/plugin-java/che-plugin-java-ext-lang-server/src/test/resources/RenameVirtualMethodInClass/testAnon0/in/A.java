package p;
//renaming A.m to k
public class A {
	void m(){}
}
class B{
	void f(){
		A a= new A(){
			void m(){
			}
		};
	}
}