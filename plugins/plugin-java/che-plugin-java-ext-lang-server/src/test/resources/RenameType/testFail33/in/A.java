package p;
//2 occurences
//disallow - shadowing
public class A {
}

class C {
	void m() {
		class B {
		}
		new A();
	}
}
