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
package test5;
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
    void m(){
        a.f= 0;
    }
}
