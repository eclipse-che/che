//no ref update
package p;
public class A {
	static int g= 0;
	void m(){
		p.A.f= 0; /**/
	}
}