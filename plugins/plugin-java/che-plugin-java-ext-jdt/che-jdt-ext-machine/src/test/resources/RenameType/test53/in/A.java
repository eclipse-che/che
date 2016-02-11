//no ref update
package p;
public class A{
	A(){}
	A(A A){}
	A m(){
		return (A)new A();
	}
};