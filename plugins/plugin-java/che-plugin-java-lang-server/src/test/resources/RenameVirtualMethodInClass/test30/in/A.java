//renaming A.m to k 
package p;

class B{
	A f(){
		return null;
	}
}

class A{
	B m(){
		return null;
	}
}

class C{
	void f(B b){
		b.f().m().f().m();
	}
}