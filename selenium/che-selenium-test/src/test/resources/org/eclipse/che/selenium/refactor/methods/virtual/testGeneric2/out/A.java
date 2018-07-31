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
