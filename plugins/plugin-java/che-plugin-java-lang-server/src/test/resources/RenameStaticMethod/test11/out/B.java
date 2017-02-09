package b;

import a.A;

public class B
{
   I i = new I()
   {
	  public void method()
	  {
		 A.fred();
	  }
   };
}

interface I
{
   void method();
}
