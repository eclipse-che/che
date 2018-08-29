//can't rename I.m to k
package p;
interface I{
	void m();
}
class C1 {
	static void k(){}
}
abstract class C2 extends C1 implements I{
	abstract public void m();
}
