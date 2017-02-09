package p;
//renaming A.m would require renaming a native method
class A {
	void m(){}
}
class B extends A{
	native void m();
}