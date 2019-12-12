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
#include "iseven.h"

void test(int);

int main() {
    int x   =   4;
  std::cout << "Hello World!" << std::endl;
  std::cout << isEven(x) << std::endl;
  return 0;
}
