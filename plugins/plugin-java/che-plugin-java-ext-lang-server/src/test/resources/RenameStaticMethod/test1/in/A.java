package p;
class A{
	static void m(){
	}
	void f(){
		m();
	}
	static int fred(){
		m();
		return 0;
	}
}
class B{
	static void m(){
		A.m();
		new A().m();
	}
}