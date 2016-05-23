//renaming I.m to k
package p;
class B {
	public void k(){};
} 
class A extends B implements I{
	public void k(){};
}
interface I {
	void k();
}