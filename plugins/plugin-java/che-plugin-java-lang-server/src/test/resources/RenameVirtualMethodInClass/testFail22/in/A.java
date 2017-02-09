package p;
//can't rename m to k - defined in subclass
class A {
	void m(){
	}
}
class B extends A{
	private static void k(){
	}
}