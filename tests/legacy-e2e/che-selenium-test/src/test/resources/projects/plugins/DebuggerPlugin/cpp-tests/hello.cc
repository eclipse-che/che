/**
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

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
