package p;
//can't rename A.m to k - duplicate
class A implements I{
	static void k(){}
	public void m(){
	}
}
interface I{
void m();
}