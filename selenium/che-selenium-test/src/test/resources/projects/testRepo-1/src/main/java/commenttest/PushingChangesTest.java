/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
//1409274557295_<!--change content-->

package commenttest;

import java.util.ArrayList;

final class PushingChangesTest
{
   private ArrayList<Integer> numbers = new ArrayList<Integer>();

   public PushingChangesTest()
   {
      numbers.add(1);
      numbers.add(2);
      numbers.add(3);
      numbers.add(4);
      numbers.add(5);
      numbers.add(6);
   }

   public ArrayList<Integer> getNumbers()
   {
      return numbers;
   }

   public Integer sum(Integer x, Integer y)
   {
      return x + y;
   }

   public Integer subtraction(Integer x, Integer y)
   {
      return x - y;
   }
}
