/*
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
package renameMethodsInInterface.testAnnotation1;
class A {
    public static final int value= 12;

    @I
    boolean f1;
    @I(number = 1)
    boolean f2;
    @I(number = value)
    boolean f3;
    @I(number=1)
    boolean f4;
    @I(number=1, x=2)
    boolean f5;
    @I(x=2)
    boolean f6;
}

@interface I {
    int x() default 0;
    int number() default 0;
}
