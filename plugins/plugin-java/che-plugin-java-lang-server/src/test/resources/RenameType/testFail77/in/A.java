package p;
class A{
}
class X{
	void m(){
		class B{}
		class C{
			Object m(Object o){
				return (A)o;
			}
		}	
	}
}