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
// Simple Hello World

#include <iostream>
using namespace std;

class Hello {
  public:
  string sayHello(string);
};

string Hello::sayHello(string name) {
  return "Hello World, " + name + "!";
}

int main()
{
  Hello hello;
  std::cout << hello.sayHello("man") << std::endl;
  return 0;
}
