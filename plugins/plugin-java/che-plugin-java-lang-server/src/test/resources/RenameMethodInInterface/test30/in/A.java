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
}
interface K{
void m();
}