package p;
//2 occurences
//disallow - obscuring
public class A {
	static int length= 1;
}

class C {
	int[] B= {4};
	void m() {
		A.length= 0;
	}
}