package p;
//can't rename A.m to k
class A{
	void m(){};
	class B {
		void k(){
			m();
		}
	}
}
