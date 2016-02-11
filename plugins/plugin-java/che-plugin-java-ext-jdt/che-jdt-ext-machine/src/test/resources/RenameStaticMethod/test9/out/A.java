//no ref update
package p;
//renaming A.m to k
class A{
	static int k(int m){
		return m(m(m));
	}
}

class B extends A{
	static int m(int m){
		return m(m(m));
	}
}