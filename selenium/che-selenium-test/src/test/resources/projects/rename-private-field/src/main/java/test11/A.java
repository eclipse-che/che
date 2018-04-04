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
package test11;
import Test.Element;

import java.util.List;

class Test {
    static class Element{
    }

    static class A {
        private final List<Element> fList;

        public A(List<Element> list) {
            fList= list;
        }
        public List<Element> getList() {
            return fList;
        }
        public void setList(List<Element> newList) {
            fList= newList;
        }
    }

    {
        A a= new A(new List<Element>());
        a.setList(a.getList());
    }
}
