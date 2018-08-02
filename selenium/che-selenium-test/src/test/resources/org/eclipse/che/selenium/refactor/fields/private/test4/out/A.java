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
    //use getMe and setMe to update fYou
    private int fYou;

    public int getMe() {
        return fYou;
    }

    /** @param me stored into {@link #fYou}*/
    public void setMe(int me) {
        fYou= me;
    }
}
