//rename X to XYZ
package p;
public class A{
	class XYZ{
		XYZ(XYZ X){new XYZ(null);}
	}
	A(){}
	A(A A){}
	A m(){
		new XYZ(null);
		return (A)new A();
	}
};
class B{
	A.XYZ ax= new A().new XYZ(null);
}