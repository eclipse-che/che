package p;

public class WhyNot {
	
	class A {	}
	class B {	}
	
	void foo() {
		A a= new A() {
			public void inA(WhyNot whyNot) {}
		};
		
		B b= new B() {
			public void inB(WhyNot whyNot) {}
		};
	}
}
