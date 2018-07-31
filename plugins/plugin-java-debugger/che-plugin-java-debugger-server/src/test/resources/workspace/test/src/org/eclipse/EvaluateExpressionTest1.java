/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse;

public class EvaluateExpressionTest1 {
    public static void main(String[] args) throws Exception {
        int i = 1;
        do1(i);
    }

    private static void do1(int i) {
        i = 2;
        System.out.println("Set breakpoint here.");
    }
}

