//renaming I.m to k
package p;
interface I{
	void k();
}
interface J{
	void k();
}
interface I1 extends I{
}
interface J1 extends J{
}
interface I2 extends I1, J1{
	void k();
}