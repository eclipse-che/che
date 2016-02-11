//renaming A.m to k 
package p;

class B{
	A f(){
		return null;
	}
}

class A{
	B k(){
		return null;
	}
}

class C{
	void f(B b){
		b.f().k().f().k();
	}
}