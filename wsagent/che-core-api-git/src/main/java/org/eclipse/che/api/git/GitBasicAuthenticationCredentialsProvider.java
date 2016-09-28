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
package org.eclipse.che.api.git;

import com.google.inject.Singleton;
import org.eclipse.che.api.git.shared.ProviderInfo;

/**
 * Credentials provider for Git basic authentication
 *
 * @author Yossi Balan
 */
@Singleton
public class GitBasicAuthenticationCredentialsProvider implements CredentialsProvider {

    private static ThreadLocal<UserCredential> currRequestCredentials = new ThreadLocal<>();
    private static final String BASIC_PROVIDER_NAME = "basic";

    @Override
    public UserCredential getUserCredential() {
        return currRequestCredentials.get();
    }

    @Override
    public String getId() {
        return BASIC_PROVIDER_NAME;
    }

    @Override
    public boolean canProvideCredentials(String url) {
        return getUserCredential() != null;
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return new ProviderInfo(BASIC_PROVIDER_NAME);
    }

    public static void setCurrentCredentials(String user, String password) {
        UserCredential creds = new UserCredential(user, password, BASIC_PROVIDER_NAME);
        currRequestCredentials.set(creds);
    }

    public static void clearCredentials() {
        currRequestCredentials.set(null);
    }

}
