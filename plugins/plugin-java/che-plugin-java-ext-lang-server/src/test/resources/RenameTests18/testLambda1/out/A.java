package p;
	@FunctionalInterface
	interface I {
		int foo (int x);
	}

	public class A {
		I i= (int f) -> {
			int /*[*/renamedP/*]*/= 10;
			return ii.foo(f) + renamedP;
		};
	}
