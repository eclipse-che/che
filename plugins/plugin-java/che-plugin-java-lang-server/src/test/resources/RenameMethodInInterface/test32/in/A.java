package p;
interface I{
void m();
}
class T implements I{
	public void m(){
		class X implements I{
			public void m(){}
		};
		X x= new X();
		x.m();
		m();
	}
}