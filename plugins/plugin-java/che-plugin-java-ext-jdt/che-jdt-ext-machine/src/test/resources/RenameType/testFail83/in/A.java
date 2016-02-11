//can't rename A to Cloneable
package p;
interface A{}
class X{
	void m(){
		class C implements A, Cloneable{}
	}
}