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
