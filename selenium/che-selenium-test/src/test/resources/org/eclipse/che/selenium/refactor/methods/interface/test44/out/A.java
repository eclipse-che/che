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
package renameMethodsInInterface.test44;
interface I {
    void k();
}
interface J{
    void k();
}
interface J2 extends J{
    void k();
}

class A{
    public void k(){};
}
class C extends A implements I, J{
    public void k(){};
}
class Test{
    void k(){

    }
}
