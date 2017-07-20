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
package test12;

class A {
    private A feel;

    /**
     * @see A # feel
     * @see A # feel
     * @see #feel
     */
    A(A a) {
        feel= a.feel;
        s\u0065tField(getField());
    }

    A get\u0046ield() {
        return feel;
    }

    public void setField(A field) {
        this./*TODO: create Getter*/feel= \u0066ield;
    }
}
