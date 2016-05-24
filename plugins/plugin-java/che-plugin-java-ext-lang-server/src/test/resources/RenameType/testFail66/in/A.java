package p;
class A{
}
class X{
	void m(){
		class B{};
		class C{
			A m(){
				return null;
			}
		}
	}
}