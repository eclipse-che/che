package p;
interface I{
void k();
}
class T{
	void m(){
		class X implements I{
			public void k(){}
		};
		X x= new X();
		x.k();
	}
}