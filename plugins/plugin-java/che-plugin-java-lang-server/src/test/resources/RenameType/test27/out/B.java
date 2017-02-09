package p;
class Super{
  int x;
}
class B extends Super{
  String x;
  class Inner{
    void m(){
      B.super.x++;
    }
  }
}
