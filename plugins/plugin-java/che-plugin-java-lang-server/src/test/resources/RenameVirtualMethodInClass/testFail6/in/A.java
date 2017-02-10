package p;
//renaming m in A would require renaming it in I
class A implements I{
	void m(){}
}

interface I {
	void m();
}