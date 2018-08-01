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
package renameMethodsInInterface.test44;
interface I {
    void m();
}
interface J{
    void m();
}
interface J2 extends J{
    void m();
}

class A{
    public void m(){};
}
class C extends A implements I, J{
    public void m(){};
}
class Test{
    void k(){

    }
}
