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
package renameStaticMethods.test2;
class A{
    static void m(){
    }
    void f(){
 m();
    }
    static int fred(){
 m();
 return 1;
    }
    {
 A.m();
 m();
 new A().m();
    }
    static {
 A.m();
 m();
 new A().m();
    }
}
class D{
    static void m(){
 A.m();
 new A().m();
 m();
    }
    static {
 A.m();
 new A().m();
 m();
    }
    {
 A.m();
 new A().m();
 m();
    }
}
