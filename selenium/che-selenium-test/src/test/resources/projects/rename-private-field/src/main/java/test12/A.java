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
package test12;

class A {
    private A fi\u0065ld;

    /**
     * @see A # field
     * @see A # fiel\u0064
     * @see #fiel\u0064
     */
    A(A a) {
        \u0066ield= a.field;
        s\u0065tField(getField());
    }

    A get\u0046ield() {
        return \u0066i\u0065ld;
    }

    public void setField(A field) {
        this./*TODO: create Getter*/field= \u0066ield;
    }
}
