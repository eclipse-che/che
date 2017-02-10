package p;


enum A {
	ONE(1), TWO(2), THREE(3)
	;
	
	int fOrdinal;
	private A(int value) {
		fOrdinal= value;
	}
}

class U {
	int one= A.ONE.fOrdinal;
}