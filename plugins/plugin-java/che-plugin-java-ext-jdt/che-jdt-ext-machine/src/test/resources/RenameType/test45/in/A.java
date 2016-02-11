//renaming A to B
package p;
 class A {
	static A A;
}
class X extends p.A{
	void x(){
		p.A.A= A.A;
	}
}