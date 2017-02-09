//rename X to XYZ - no ref update
package p;
public class A{
	class XYZ{
		XYZ(X X){new X(null);}
	}
	A(){}
	A(A A){}
	A m(){
		new X(null);
		return (A)new A();
	}
};
class B{
	A.X ax= new A().new X(null);
}