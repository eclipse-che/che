package p;

class A {
	A fi\u0065ld;
	
	/**
	 * @see A # field
	 * @see A # fiel\u0064
	 * @see #fiel\u0064
	 */
	A(A a) {
		\u0066ield= a.field;
		s\u0065tField(getField());
	}
	
	A get\u0046ield() {
		return \u0066i\u0065ld;
	}
	
	public void setField(A field) {
		this./*TODO: create Getter*/field= \u0066ield;
	}
}