//renaming A.m to k 
package p;

class A{
	void m(){
		System.out.println("a");
	}
	class B{
		class C {
			void f(){
				m();
			}
		}	
	}
}