package p;
class A extends Exception {
}
class X {
	void f() {
		class B extends Exception {
		};
		class C {
			void m() throws A {
			}
			void k() {
				try {
					m();
				} catch (A a) {
				}
			}
		}
	}
}
