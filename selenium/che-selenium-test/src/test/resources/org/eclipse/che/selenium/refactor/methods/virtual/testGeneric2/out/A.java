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
package renameVirtualMethods.testGeneric2;
class A<E>{
    public boolean addIfPositive(E e) {
        return true;
    }
}

class Sub<E extends Number> extends A<E> {
    public boolean addIfPositive(E e) {
        if (e.doubleValue() > 0)
            return false;
        return super.addIfPositive(e);
    }
}

class Unrelated<E> {
    public boolean add(E e) {
        return false;
    }
}
