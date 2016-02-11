package p;
interface I{
  int A = 0;
}
class B{
  int A = I.A; 
}