//no ref update
package p;
public class B{
	B(){}
	B(A A){}
	A m(){
		return (A)new A();
	}
};