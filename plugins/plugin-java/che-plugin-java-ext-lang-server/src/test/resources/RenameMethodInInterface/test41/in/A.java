package p;
//renaming I.m to k
interface I{
	void m();
}
interface I2{
	void m();
	void k();
	
}
class A{
}
class B extends A implements I{
public void m(){}
}
class C extends A implements I2{
public void m(){}
public void k(){}

}