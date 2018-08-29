package p;
//renaming A.m to k
class A {
	private void m(){}
	class B{
		void f(){
			m();
		}
	}
}
