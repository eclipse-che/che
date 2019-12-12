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
package renameVirtualMethods.testVarArgs1;
public class A {
    public String runall(Runnable[] runnables) { return "A"; }

    public static void main(String[] args) {
        Runnable r1 = null, r2 = null;
        System.out.println(new A().runall(new Runnable[] { r1, r2 }));
        System.out.println(new Sub().runall(new Runnable[] { r1, r2 }));
        System.out.println(new Sub().runall(r1, r2));
        System.out.println(new Sub2().runall(new Runnable[] { r1, r2 }));
    }
}

class Sub extends A {
    public String runall(Runnable... runnables) { return "Sub, " + super.runall(runnables); }
}

class Sub2 extends Sub {
    public String runall(Runnable[] runnables) { return "Sub2, " + super.runall(runnables); }
}
