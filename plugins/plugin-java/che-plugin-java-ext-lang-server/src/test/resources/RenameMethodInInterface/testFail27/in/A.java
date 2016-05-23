package p;
interface I{
void m();
}
class A implements I, J{
public void m(){};
}
interface J{
void m();
}
class B implements J, K{
public void m(){};
public void k(){};

}
interface K{
void m();
void k();
}