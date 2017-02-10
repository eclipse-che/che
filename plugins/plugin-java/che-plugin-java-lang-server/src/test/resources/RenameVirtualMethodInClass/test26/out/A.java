//renaming A.m to k 
package p;

class A{
	void k(){}
}
class B{
	static {
		new A().k();
	}
}