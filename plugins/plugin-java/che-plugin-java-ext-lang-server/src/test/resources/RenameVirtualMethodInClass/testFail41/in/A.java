package p;
import static p.B.k;

class A {
	void m(){ }
}

class B {
	public static void k() {}
}

class C {
	class I extends A {
		{
			k();
		}
	}
}