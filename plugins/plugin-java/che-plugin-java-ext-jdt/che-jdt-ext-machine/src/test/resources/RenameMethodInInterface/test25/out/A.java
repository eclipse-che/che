package p;
interface J{
}
interface I extends J{
void k();
}
interface I1 extends J{
void k();
}
class C implements I, I1{
public void k(){}

}