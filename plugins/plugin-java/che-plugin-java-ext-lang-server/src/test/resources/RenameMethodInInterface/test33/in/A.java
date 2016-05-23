//renaming I.m to k
package p;
interface I{
void m();
}
class A1 implements I, I1{
public void m(){}
}
interface I1{
void m();
}
class A2 implements I1, I2{
public void m(){}
}
interface I2{
void m();
}
class A3 implements I3, I2{
public void m(){}
}
interface I3{
void m();
}