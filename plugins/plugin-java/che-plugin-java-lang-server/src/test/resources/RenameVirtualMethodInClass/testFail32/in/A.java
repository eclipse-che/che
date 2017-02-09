//can't rename A.m to k
package p;
public class A {
	void m(Object m){
		System.out.println("A");
	}
}
class B extends A{
	void k(String m){
		System.out.println("B");
	}
}