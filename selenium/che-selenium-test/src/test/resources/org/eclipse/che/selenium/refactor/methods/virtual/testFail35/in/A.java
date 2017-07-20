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
package renameVirtualMethods.testFail35;
public class A {
    void m(String m){
 System.out.println("A");
    }
}
class B extends A{
    void k(Object m){
 System.out.println("B");
    }
}
