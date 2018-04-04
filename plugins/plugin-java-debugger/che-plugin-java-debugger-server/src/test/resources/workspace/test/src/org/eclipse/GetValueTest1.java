/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse;

public class GetValueTest1 {
    private              String var1 = "field1";
    private static final String var2 = "field2";

    public static void main(String[] args) throws Exception {
        do1(1);
    }

    private static void do1(int i) {
        String var1 = "var1";
        String var2 = "var2";
        i = 2;

        System.out.println("Set breakpoint here.");
    }
}

