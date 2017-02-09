package p;

public abstract class A {
	abstract void m();
}

class B extends A {
	void m() {
		//Foo
	}
}

class C extends B {
	void m() {
		//Bar
		C c= new C() {
			void m() {
				// X
			}
		};
	}
}
