package p;
//can't rename I.m to k - duplicate
interface I {
	void m();
}
class B implements I{
	public void m(){}
	private int k();
}