//renaming I.m to k
package p;
interface I{
	void k();
}
interface J{
	void k();
}
class C implements I, J{
	public void k(){};
}