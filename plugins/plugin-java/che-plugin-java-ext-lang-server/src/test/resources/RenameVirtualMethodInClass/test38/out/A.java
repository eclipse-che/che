//rename A#m() to k() -> must rename all m()
package p;
abstract class Abstract {
	public abstract void k();
	void caller(Abstract abstr, A a, Interface inter, Impl2 impl2) {
		abstr.k();
		a.k();
		inter.k();
		impl2.k();
	}
}

class A extends Abstract {
	public void k() { // from Abstract
	}
}

interface Interface { //independent of Abstract
	void k();
}

class Impl2 extends Abstract implements Interface {
	public void k() { // from Abstract AND from Interface
	}
}
