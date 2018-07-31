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
package test6;
class A{
    protected int f;
    void m(){
        f++;
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
        a.f= 0;
        b.f= 0;
        ab.f= 0;
    }
}
