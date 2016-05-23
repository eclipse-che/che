package p;
//can't rename m to k - defined in subclass
class A {
	void m(){
	}
}
class B extends A{
	static void k(){
	}
}
