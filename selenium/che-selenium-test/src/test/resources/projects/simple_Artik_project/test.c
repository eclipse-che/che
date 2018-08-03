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
#include <stdio.h>

int main() {
 int integer1; // Declare a variable named integer1 of the type integer
 int integer2; // Declare a variable named integer2 of the type integer
 int sum;      // Declare a variable named sum of the type integer
 int multiplyVal;

 integer1 = 55;   // Assign value to variable integer1
 integer2 = 66;   // Assign value to variable integer1
 multiplyVal = 0;
 sum = integer1 + integer2;   // Compute the sum
 multiplyVal  = multiply(integer1, integer2);
 // Print the result
 printf("The sum of %d and %d is %d.\n", integer1, integer2, sum);

 return 0;
}


int multiply(int a, int b){
return a*b;
}
