package p;
//renaming A.m to k
class A{
	void m(){
	}
}
class test{
	void m(){
		class X extends A{
			void m(){}
		}
	}	
}