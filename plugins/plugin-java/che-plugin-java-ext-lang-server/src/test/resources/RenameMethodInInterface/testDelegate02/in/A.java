package p;

public interface I {
	public void m();
}

interface B extends I {
}

interface C extends B {
	public void m();
}
