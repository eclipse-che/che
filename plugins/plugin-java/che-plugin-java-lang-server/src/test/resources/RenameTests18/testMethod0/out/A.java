package p;
@FunctionalInterface
interface I {
	int k();
}

public class A {
	I i1= () -> 2;
}
