package p;
class A{
	/**
	 * @see #getMe()
	 * @see #setMe(int)
	 */
	private int fMe; //use getMe and setMe to update fMe
	
	public int getMe() {
		return fMe;
	}
	
	/** @param me stored into {@link #fMe}*/
	public void setMe(int me) {
		fMe= me;
	}
}