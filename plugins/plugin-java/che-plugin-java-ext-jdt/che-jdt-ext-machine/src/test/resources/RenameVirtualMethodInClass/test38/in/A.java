//rename A#m() to k() -> must rename all m()
package p;
abstract class Abstract {
	public abstract void m();
	void caller(Abstract abstr, A a, Interface inter, Impl2 impl2) {
		abstr.m();
		a.m();
		inter.m();
		impl2.m();
	}
}

class A extends Abstract {
	public void m() { // from Abstract
	}
}

interface Interface { //independent of Abstract
	void m();
}

class Impl2 extends Abstract implements Interface {
	public void m() { // from Abstract AND from Interface
	}
}
