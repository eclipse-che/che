package p;
class A{
  int x;
  class Inner{
    void m(){
      A.this.x++;
    }
  }
}