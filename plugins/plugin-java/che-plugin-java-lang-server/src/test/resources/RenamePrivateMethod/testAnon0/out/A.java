package p;
//renaming A.m to k
public class A {
	private void k(){}
}
class B{
	void f(){
		A a= new A(){
			void m(){
			}
		};
	}
}