package p;

public class A {
	I i1 = () -> {};
}

@FunctionalInterface
interface I {
	void k();
}

class Test {
	void k(){}	
}