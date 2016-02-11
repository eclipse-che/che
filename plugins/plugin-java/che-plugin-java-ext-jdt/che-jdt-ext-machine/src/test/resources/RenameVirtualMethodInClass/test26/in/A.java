//renaming A.m to k 
package p;

class A{
	void m(){}
}
class B{
	static {
		new A().m();
	}
}