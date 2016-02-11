package p;


enum A {
	ONE(1), TWO(2), THREE(3)
	;
	
	int fValue;
	private A(int value) {
		fValue= value;
	}
}

class U {
	int one= A.ONE.fValue;
}