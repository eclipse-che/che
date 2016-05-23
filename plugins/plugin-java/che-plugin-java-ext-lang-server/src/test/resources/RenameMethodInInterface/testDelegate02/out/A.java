package p;

public interface I {
	/**
	 * @deprecated Use {@link #k()} instead
	 */
	public void m();

	public void k();
}

interface B extends I {
}

interface C extends B {
	/**
	 * @deprecated Use {@link #k()} instead
	 */
	public void m();

	public void k();
}
