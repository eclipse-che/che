package p;

class A {
	A feel;
	
	/**
	 * @see A # feel
	 * @see A # feel
	 * @see #feel
	 */
	A(A a) {
		feel= a.feel;
		setFeel(getFeel());
	}
	
	A getFeel() {
		return feel;
	}
	
	public void setFeel(A field) {
		this./*TODO: create Getter*/feel= \u0066ield;
	}
}