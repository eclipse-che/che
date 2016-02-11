//renaming I.m to k
package p;
interface I{
void k();
}
class A{
public void k(){}
}
class B1 extends A implements I{
}
class B2 extends A {
public void k(){}
}