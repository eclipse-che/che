package p;
interface I{
void k();
}
class A{
public void k(){};
}
class B1 extends A implements J{
}
class B2 extends A implements I{
}
interface J{
void k();
}
class C implements J{
public void k(){};
}