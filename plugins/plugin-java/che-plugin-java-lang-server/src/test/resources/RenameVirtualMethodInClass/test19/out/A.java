//renaming A.m to fred
package p;
public class A{
	void fred() {
	}
}

class B{
	void k(){
		A a= new A();
		a.fred();
	}
}