package p;
class A{
	static void m(){
	}
	void f(){
		m();
	}
	static int fred(){
		m();
		return 1;
	}
	{
		A.m();
		m();
		new A().m();
	}
	static {
		A.m();
		m();
		new A().m();
	}
}
class D{
	static void m(){
		A.m();
		new A().m();
		m();
	}
	static {
		A.m();
		new A().m();
		m();
	}
	{
		A.m();
		new A().m();
		m();
	}
}