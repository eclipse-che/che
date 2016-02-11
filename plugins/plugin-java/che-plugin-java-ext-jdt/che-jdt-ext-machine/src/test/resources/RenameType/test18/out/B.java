package p;
class B extends Exception{
}
class AA{
  void m(){
    try {
      throw new B();
    }
    catch(B a){}
  }
}