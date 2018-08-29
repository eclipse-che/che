package p;
@FunctionalInterface
interface I {
	int m();
}

public class A {
	I i1= () -> 2;
}
