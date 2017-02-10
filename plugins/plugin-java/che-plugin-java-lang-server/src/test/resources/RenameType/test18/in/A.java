package p;
class A extends Exception{
}
class AA{
  void m(){
    try {
      throw new A();
    }
    catch(A a){}
  }
}