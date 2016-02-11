package p;
interface J{
}
interface I extends J{
void k();
}
interface I1 extends J{
void k();
}
interface K extends I, I1{
}