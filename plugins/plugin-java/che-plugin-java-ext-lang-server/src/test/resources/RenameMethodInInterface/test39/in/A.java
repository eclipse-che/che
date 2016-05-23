//renaming I.m to k
package p;
interface I{
void m();
}
interface I2{
void m();
}
interface I3 extends I{
}
interface I4 extends I3, I2{
}