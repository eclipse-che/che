package p;
class B{
  int x;
  class Inner{
    void m(){
      B.this.x++;
    }
  }
}