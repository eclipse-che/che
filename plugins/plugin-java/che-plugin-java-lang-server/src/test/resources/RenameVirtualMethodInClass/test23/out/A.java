package p;
//renaming A.m to k
class A{
	void k(){
	}
}
class test{
	void k(){
		class X extends A{
			void k(){}
		}
	}	
}