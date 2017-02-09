package p;
class A{
	protected int g;
	void m(){
		g++;
	}
}

class AA extends A{
	protected int f;
}

class B{
	A a;
	void m(){
		a.g= 0;
	}
}