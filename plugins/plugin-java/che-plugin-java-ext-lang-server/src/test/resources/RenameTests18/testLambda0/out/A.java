package p;
	@FunctionalInterface
	interface I {
		int foo (int x);
	}

	public class A {
		I i= (int /*[*/renamedF/*]*/) -> {
			int p= 10;
			return ii.foo(renamedF) + p;
		};
	}
