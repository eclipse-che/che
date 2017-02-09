package p;
//renaming A.m to k
class A {
	static void k(){}
	class B{
		void f(){
			k();
		}
	}
}
