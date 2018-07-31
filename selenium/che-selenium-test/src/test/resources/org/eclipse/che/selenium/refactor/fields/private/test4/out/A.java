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
