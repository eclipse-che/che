package p;
class Super{
  void m1(){};
}
class B extends Super{
  class Inner{
    void m(){
      B.super.m1();
    }
  }
}
