//renaming I.m to k
package p;
interface I{
	void m();
}
interface J{
	void m();
}
class A{
	public void m(){};
}
class C extends A implements I, J{
	public void m(){};
}