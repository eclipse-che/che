//can't rename A.m to k
package p;
public class A implements I{
	public void m(String m){
		System.out.println("A");
	}
}
class B extends A{
	public void k(Object m){
		System.out.println("B");
	}
}
interface I{
void m(String k);
}