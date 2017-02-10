package p;
class A{
	protected int g;
	void m(){
		g++;
	}
}
class B extends A{
	void m(){
		g= 0;
	}
}