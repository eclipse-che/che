package p;
class A {
	A(A A){}
	A A(A A){
		A= new A(new A(A));
		return A;
	}
}