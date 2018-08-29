package p;
class Super{
  void m1(){};
}
class A extends Super{
  class Inner{
    void m(){
      A.super.m1();
    }
  }
}
