//renaming A to B
package p;
 class B {
	static B A;
}
class X extends p.B{
	void x(){
		p.B.A= A.A;
	}
}