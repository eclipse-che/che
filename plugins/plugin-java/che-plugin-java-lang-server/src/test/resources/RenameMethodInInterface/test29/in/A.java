package p;
interface I{
void m();
}
class A{
public void m(){};
}
class B1 extends A implements J{
}
class B2 extends A implements I{
}
interface J{
void m();
}
class C implements J{
public void m(){};
}