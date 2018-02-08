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

public class StackFrameDumpTest1 {
    private static String v = "something";
    public static void main(String[] args) throws Exception {
        do1(1);
    }

    private static void do1(int i) {
        int j = 1;
        do2(String.valueOf(i + j));
    }

    private static void do2(String str) {
        System.out.println(str);
    }
}
