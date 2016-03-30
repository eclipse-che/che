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
package org.eclipse.che.plugin.svn.server.credentials;

import org.eclipse.che.commons.annotation.Nullable;

public interface CredentialsProvider {

    @Nullable
    Credentials getCredentials(String repositoryUrl) throws CredentialsException;

    void storeCredential(String repositoryUrl, Credentials credentials) throws CredentialsException;

    public class Credentials {

        private final String username;
        private final String password;

        public Credentials(final String username, final String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

}
