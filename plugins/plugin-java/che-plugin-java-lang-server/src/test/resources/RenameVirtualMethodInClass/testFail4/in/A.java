package p;
//renaming m in A would require renaming it in I
class A{
 public void m(){}
}

class B extends A implements I{
}

interface I {
	void m();
}