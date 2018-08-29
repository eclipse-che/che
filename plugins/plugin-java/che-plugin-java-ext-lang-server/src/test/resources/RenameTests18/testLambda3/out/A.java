package p;
	@FunctionalInterface
	interface I {
		int foo (int x);
	}

	public class A {
		I i1= (x_renamed) -> {
			x_renamed++;
			return /*[*/x_renamed/*]*/;
		     };
	}
