package p;
interface I{
void k();
}
class T implements I{
	public void k(){
		class X implements I{
			public void k(){}
		};
		X x= new X();
		x.k();
		k();
	}
}