//can't rename I.m to k
package p;
interface I{
	void m();
}
class C1 {
	static void k(){}
}
class C2 extends C1 implements I{
	public void m(){}
}
