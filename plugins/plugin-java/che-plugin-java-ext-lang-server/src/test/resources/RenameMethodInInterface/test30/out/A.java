package p;
interface I{
void k();
}
class A implements I, J{
public void k(){};
}
interface J{
void k();
}
class B implements J, K{
public void k(){};
}
interface K{
void k();
}