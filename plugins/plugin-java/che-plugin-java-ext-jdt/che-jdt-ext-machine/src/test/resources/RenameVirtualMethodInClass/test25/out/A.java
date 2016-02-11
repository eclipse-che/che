//renaming A.m to k 
package p;

class A{
	void k(){
		System.out.println("a");
	}
	class B{
		class C {
			void f(){
				k();
			}
		}	
	}
}