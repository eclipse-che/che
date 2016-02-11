//renaming I.m to k
package p;
interface I{
void m();
}
class A{
public void m(){}
}
class B1 extends A implements I{
}
class B2 extends A {
public void m(){}
}