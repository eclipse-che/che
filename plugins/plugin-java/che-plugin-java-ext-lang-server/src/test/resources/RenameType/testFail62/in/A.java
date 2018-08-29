package p;
class A{
	static int x;
}
class X{
	static class B{
		static int x;
	}
	int m(Object o){
		return A.x;
	}
}
