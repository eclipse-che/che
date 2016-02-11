package p;
class A<T>{
	private T f;
	class B<T>{
		static <S> T f(T t) {
			T s=t;
			return null;
		}
	}
}