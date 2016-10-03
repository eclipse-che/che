/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.subversion;

/**
 * Credentials object for subversion operations.
 *
 * @author Igor Vinokur
 */
public class Credentials {
    private String userName;
    private String password;

    /**
     * Returns user name for authentication.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set user name for authentication.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns password for authentication.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set password for authentication.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
