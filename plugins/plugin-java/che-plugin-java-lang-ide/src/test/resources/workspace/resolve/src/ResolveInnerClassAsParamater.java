public class ResolveInnerClassAsParamater {
	class Inner {
	}
	void foo(String s) {
	}
	void foo(Inner i) {
	}
	void bar(Inner i) {
		foo(i);
	}
}