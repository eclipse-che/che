package p;
class B{
   static void s(){};
}
class AA{
   AA(){ 
     B.s();
   };   
}