//nor ref update
package p;
//renaming I.m to k
interface I {
void k();
}
class C1 implements I{
	public void k(){};
}
class D{
	void h(){
		I a= new C1();
		a.m();
	}
}
class NotRel{
	public void m(){};
	void f(){
		m();
	}
}