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
package test11;
import Test.Element;

import java.util.List;

class Test {
    static class Element{
    }

    static class A {
        private final List<Element> fElements;

        public A(List<Element> list) {
            fElements= list;
        }
        public List<Element> getList() {
            return fElements;
        }
        public void setList(List<Element> newList) {
            fElements= newList;
        }
    }

    {
        A a= new A(new List<Element>());
        a.setList(a.getList());
    }
}
