package p;
//renaming A.m to k
class B{
	private void m(){
	}
	void f(){
		m();
	}
}
class A extends B{
	private void m(){
	}
}