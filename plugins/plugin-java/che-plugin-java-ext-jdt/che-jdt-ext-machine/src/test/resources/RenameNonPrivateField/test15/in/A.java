//no ref update
package p;
public class A {
	public int f= 0;
	void m(){
		f= 0; /**/
	}
}
class B{
	void m(){
		new A().f= 0;
	}
}