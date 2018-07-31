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
package test3;
/**
 * @see #g
 * @see A#g
 * @see test3.A#g
 * @see B#f
 */
class A{
    protected int g;
    void m(){
        g++;
    }
}
/**
 * @see #f
 */
class B{
    A a;
    protected int f;
    void m(){
        a.g= 0;
    }
}
