package p;
class A extends Exception{
}
class X{
	class B extends Exception{
	}
	void m() throws A{
	}
	void k(){
		try {
			m();
		} catch (A a){
		}
	}
}