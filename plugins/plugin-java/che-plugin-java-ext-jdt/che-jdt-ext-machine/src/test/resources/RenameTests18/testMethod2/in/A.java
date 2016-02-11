package p;

public class A {
	I i1 = () -> {};
}

@FunctionalInterface
interface I {
	void m();
}

class Test {
	void k(){}	
}