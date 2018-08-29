package p;

class A {
   
	public String g;

	/**
	 * @deprecated Use {@link #getG()} instead
	 */
	public String getF() {
		return getG();
	}

	public String getG() {
		return g;
	}

	/**
	 * @deprecated Use {@link #setG(String)} instead
	 */
	public void setF(String f) {
		setG(f);
	}

	public void setG(String f) {
		this.g = f;
	}
}
