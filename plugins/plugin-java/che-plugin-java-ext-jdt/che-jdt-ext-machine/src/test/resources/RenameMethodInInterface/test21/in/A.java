//renaming I.m to k
package p;
interface I {
	void m();
}
interface J{
	void m();
}
interface J2 extends J{
	void m();
}

class A{
	public void m(){};
}
class C extends A implements I, J{
	public void m(){};
}
class Test{
	void k(){
		I i= new C();
		i.m();
		I ii= new I(){
			public void m(){}
		};
		ii.m();
		J j= new C();
		j.m();
		J jj= new J(){
			public void m(){}
		};
		jj.m();
		A a= new C();
		((I)a).m();
		((J)a).m();
		((C)a).m();
		a.m();
	}
}