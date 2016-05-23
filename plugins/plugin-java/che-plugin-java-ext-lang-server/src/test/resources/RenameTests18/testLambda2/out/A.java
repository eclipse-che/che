package p;
@FunctionalInterface
interface I {
	int foo (int x);
}

public class C1 {
	I i= (int x) -> {
		int p= 10;
		I /*[*/renamedIi/*]*/= (int a) -> a+100;
		return renamedIi.foo(x) + p;
	};
}