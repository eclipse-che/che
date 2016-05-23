//renaming I.m to k
package p;
interface I{
void m();
}
interface I2{
void m();
}
interface I3 extends I, I2{
}
class A implements I3{
public void m(){}

}