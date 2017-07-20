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
