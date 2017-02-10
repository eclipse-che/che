//renaming I.m to k
package p;
interface I{
void k();
}
class A1 implements I, I1{
public void k(){}
}
interface I1{
void k();
}
class A2 implements I1, I2{
public void k(){}
}
interface I2{
void k();
}
class A3 implements I3, I2{
public void k(){}
}
interface I3{
void k();
}