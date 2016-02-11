package p;
public class B {
	B(B A){}
	B A(B A){
		A= new B(new B(A));
		return A;
	}
}