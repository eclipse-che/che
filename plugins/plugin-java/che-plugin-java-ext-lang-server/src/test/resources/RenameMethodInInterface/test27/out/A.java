package p;
interface I{
void k();
}
class A{
public void k(){};
}
class B1 extends A{
public void k(){};
}
class B2 extends A implements I{
}
