package p;
class A{
	static int x(){return 42;};
}
class X{
	static class B{
		static int x(){return 42;};
	}
	int m(Object o){
		return A.x();
	}
}
