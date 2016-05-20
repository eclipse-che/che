package p;
class A<T>{
	private T f;
	class B<T>{
		<S> S f(S t) {
			S s=t;
			return null;
		}
	}
}