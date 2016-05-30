package p;
class A{
	/**
	 * @see #getYou()
	 * @see #setYou(int)
	 */
	private int fYou; //use getMe and setMe to update fMe
	
	public int getYou() {
		return fYou;
	}
	
	/** @param me stored into {@link #fYou}*/
	public void setYou(int me) {
		fYou= me;
	}
}