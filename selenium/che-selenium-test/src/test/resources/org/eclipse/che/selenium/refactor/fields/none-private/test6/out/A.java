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
package test6;
class A{
    protected int g;
    void m(){
        g++;
    }
}

class AA extends A{
    protected int f;
}

class B{
    A a;
    AA b;
    A ab= new AA();
    void m(){
        a.g= 0;
        b.f= 0;
        ab.g= 0;
    }
}
