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
package org.eclipse;

public class GetValueTest1 {
    private static String v = "something";

    public static void main(String[] args) throws Exception {
        do1(1);
    }

    private static void do1(int i) {
        String v = "test";
        i = 2;
        System.out.println("Set breakpoint here.");
    }
}

