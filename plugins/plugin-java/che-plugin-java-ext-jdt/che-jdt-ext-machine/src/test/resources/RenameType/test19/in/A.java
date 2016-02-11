package p;
class A extends Exception{
  void m(){
    try {
      throw new A();
    }
    catch(A A){}
  }
}
