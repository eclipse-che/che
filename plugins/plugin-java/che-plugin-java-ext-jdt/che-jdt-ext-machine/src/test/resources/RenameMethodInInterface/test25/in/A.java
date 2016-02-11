package p;
interface J{
}
interface I extends J{
void m();
}
interface I1 extends J{
void m();
}
class C implements I, I1{
public void m(){}

}