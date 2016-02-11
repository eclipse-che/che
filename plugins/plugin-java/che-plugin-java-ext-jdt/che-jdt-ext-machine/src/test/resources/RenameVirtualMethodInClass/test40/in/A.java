package p;
public class A {
	void doit() {
		class LocalClass {
			public void method(int i) {} //rename to "method2"
		}
	}
	void method2(int i) {}
}
