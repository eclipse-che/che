package p;
//renaming I.m to k
interface I {
void m();
}
class C1 implements I{
	public void m(){};
}
class D{
	void h(){
		I a= new C1();
		a.m();
	}
}
class NotRel{
	public void m(){};
	void f(){
		m();
	}
}