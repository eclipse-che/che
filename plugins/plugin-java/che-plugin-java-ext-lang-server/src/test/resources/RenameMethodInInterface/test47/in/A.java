package p;
//renaming I.m to k
interface I {
	void m();
}
class AQ implements I{
	public void m(){}
}

class AQE extends AQ{
	public void m(){
		super.m();
	}
}
