package p;
//renaming A.m to k
class A{
	private void m(){
	}
}
class test{
	void m(){
		class X extends A{
			void m(){
		}
	}
}}