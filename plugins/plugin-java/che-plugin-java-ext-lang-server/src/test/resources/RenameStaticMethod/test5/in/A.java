package p;
//renaming A.m to k
class A {
	static void m(){}
	class B{
		void f(){
			m();
		}
	}
}
