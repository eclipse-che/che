package p;
//renaming I.m to k
interface I {
	void k();
}
class AQ implements I{
	public void k(){}
}

class AQE extends AQ{
	public void k(){
		super.k();
	}
}
