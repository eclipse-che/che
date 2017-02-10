package p;

public class Why {
	
	class A {	}
	class B {	}
	
	void foo() {
		A a= new A() {
			public void inA(Why why) {}
		};
		
		B b= new B() {
			public void inB(Why why) {}
		};
	}
}
