package p;
class B extends Exception{
  void m(){
    try {
      throw new B();
    }
    catch(B A){}
  }
}
