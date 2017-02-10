package b;

import a.A;

public class B
{
   I i = new I()
   {
	  public void method()
	  {
		 A.method2();
	  }
   };
}

interface I
{
   void method();
}
