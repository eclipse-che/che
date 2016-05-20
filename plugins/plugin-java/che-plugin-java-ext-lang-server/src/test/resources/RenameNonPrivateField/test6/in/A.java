package p;
class A{
	protected int f;
	void m(){
		f++;
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
		a.f= 0;
		b.f= 0;
		ab.f= 0;
	}
}