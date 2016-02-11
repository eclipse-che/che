package p;
class A{
	static void k(){
	}
	void f(){
		k();
	}
	static int fred(){
		k();
		return 1;
	}
	{
		A.k();
		k();
		new A().k();
	}
	static {
		A.k();
		k();
		new A().k();
	}
}
class D{
	static void m(){
		A.k();
		new A().k();
		m();
	}
	static {
		A.k();
		new A().k();
		m();
	}
	{
		A.k();
		new A().k();
		m();
	}
}