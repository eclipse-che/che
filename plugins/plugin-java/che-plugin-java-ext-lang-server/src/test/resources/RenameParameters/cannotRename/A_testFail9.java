//cannot rename to: j
package p;

class B{
	int j;
	class A {
		int m(int i){
			i= j;
			return 0;
		};
	}
}