//renaming I.m to k
package p;
interface I{
	void m();
}
interface J{
	void m();
}
class C implements I, J{
	public void m(){};
}