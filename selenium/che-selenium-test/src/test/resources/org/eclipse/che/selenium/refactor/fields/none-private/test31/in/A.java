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
package test31;

enum A {
    RED, GREEN, BLUE, YELLOW;
    A buddy;
    public A getBuddy() {
        return buddy;
    }
    public void setBuddy(A b) {
        buddy= b;
    }
}

class User {
    void m() {
        A.RED.setBuddy(A.GREEN);
        if (A.RED.getBuddy() == A.GREEN) {
            A.GREEN.buddy= null;
        }
    }
}
