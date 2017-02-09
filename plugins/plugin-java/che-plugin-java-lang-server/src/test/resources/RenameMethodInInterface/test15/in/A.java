//renaming I.m to k
package p;
interface I{
	void m();
}
interface J{
	void m();
}
interface I1 extends I{
}
interface J1 extends J{
}
interface I2 extends I1, J1{
	void m();
}