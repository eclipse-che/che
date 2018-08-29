package p;
//renaming A.m to k
class A {
	private void k(){}
	class B{
		void f(){
			k();
		}
	}
}
