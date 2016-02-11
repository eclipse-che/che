package p;
//renaming m in A would require renaming it in I
class A{
 	public void m(){}
}

class B extends A implements I2{
}


interface I {
	void m();
}

interface I2 extends I{
}