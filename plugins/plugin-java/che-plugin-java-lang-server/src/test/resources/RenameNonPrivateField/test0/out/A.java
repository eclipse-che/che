package p;
/**
 * @see #g
 * @see A#g
 * @see B#f
 */
class A{
	/**
	 * @see #g
	 */
	int g;
}

class B{
	/**
	 * @see A#g
	 */
	int f;
}