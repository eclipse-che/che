//can rename A.m to k
package p;
class A{
	void k(){
	}
}
class X{
	void k(){
	}
	class B{
		void k(){
		}
		class C extends A{
			void f(){
				k();
			}
		}	
	}
}