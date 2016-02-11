package p;
class Super{
  int x;
}
class A extends Super{
  String x;
  class Inner{
    void m(){
      A.super.x++;
    }
  }
}
