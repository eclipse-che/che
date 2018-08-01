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
package test4;
class A {
    /**
     * @see #getMe()
     * @see #setMe(int)
     */
    //use getMe and setMe to update fMe
    private int fMe;

    public int getMe() {
        return fMe;
    }

    /** @param me stored into {@link #fMe}*/
    public void setMe(int me) {
        fMe= me;
    }
}
