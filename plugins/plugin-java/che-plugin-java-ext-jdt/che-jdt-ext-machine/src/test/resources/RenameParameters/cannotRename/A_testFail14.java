//cannot rename to j
package p;
class A{
	int k;
	static class j{
		static int k;
	}
	void m(A i){
		j.k= 0;
	}
}	