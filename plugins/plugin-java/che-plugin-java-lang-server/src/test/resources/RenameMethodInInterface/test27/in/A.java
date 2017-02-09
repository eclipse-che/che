package p;
interface I{
void m();
}
class A{
public void m(){};
}
class B1 extends A{
public void m(){};
}
class B2 extends A implements I{
}
