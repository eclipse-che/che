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
	AA b;
	A ab= new AA();
	void m(){
		a.g= 0;
		b.f= 0;
		ab.g= 0;
	}
}