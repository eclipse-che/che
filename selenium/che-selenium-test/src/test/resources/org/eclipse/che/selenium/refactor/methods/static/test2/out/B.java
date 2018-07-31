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
package renameStaticMethods.test2;
class A{
    static void k(){
    }
    void f(){
 k();
    }
    static int fred(){
 k();
 return 1;
    }
    {
 A.k();
 k();
 new A().k();
    }
    static {
 A.k();
 k();
 new A().k();
    }
}
class D{
    static void m(){
 A.k();
 new A().k();
 m();
    }
    static {
 A.k();
 new A().k();
 m();
    }
    {
 A.k();
 new A().k();
 m();
    }
}
