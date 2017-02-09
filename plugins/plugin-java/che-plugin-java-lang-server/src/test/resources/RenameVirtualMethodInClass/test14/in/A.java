package p;
//renaming A.m to k
class B{
	private void k(){
	}
	void f(){
		k();
	}
}
class A extends B{
	void m(){
	}
	void f(){
		m();
	}
}