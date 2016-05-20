package p;
class A<T>{
	static class B {
		static <T> T f(T t) {
			class S {}
			return null;
		} 
	}
	private T f;
	private B g;
}