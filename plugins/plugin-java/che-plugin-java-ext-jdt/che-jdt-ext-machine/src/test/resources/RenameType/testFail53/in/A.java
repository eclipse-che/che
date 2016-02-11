package p;
class A extends Exception{
}
class X{
	class B extends Exception{
	}
	X() throws A{
	}
}