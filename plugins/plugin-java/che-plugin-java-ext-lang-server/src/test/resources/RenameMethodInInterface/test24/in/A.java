package p;
interface J{
}
interface I extends J{
void m();
}
interface I1 extends J{
void m();
}
interface K extends I, I1{
}