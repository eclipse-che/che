package p;
class A{
}
class X{
	void m(){
		class B{	}
		class C{
			boolean m(Object o){
				return o instanceof A;
			}
		}	
	}	
}