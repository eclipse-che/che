//renaming I.m to k
package p;
interface I{
void k();
}
interface I2{
void k();
}
interface I3 extends I{
}
interface I4 extends I3, I2{
}