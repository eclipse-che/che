package p;
//2 occurences
//disallow - obscuring
public class A {
	static int length= 1;
}

class C {
	void m(int[] B) {
		A.length= 0;
	}
}

