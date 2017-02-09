package p;
interface I{
void m();
}
class T{
	void m(){
		class X implements I{
			public void m(){}
		};
		X x= new X();
		x.m();
	}
}