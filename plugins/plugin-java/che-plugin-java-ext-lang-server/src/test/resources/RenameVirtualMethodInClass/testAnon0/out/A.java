package p;
//renaming A.m to k
public class A {
	void k(){}
}
class B{
	void f(){
		A a= new A(){
			void k(){
			}
		};
	}
}