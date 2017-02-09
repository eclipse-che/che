//can't rename A.m to k
package p;

class X {
	void k(){
	}
}
class A {
	private void m(){
		System.out.println("a");
	}
	class B extends X{
		void f(){
			m();
		}
		public void foo() {
			
		}
	}
}