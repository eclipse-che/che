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
package org.eclipse.che.ide.ext.git.server.nativegit;

import org.eclipse.che.api.git.CredentialsProvider;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.api.git.shared.ProviderInfo;
import org.eclipse.che.api.git.UserCredential;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Credentials provider for Che
 *
 * @author Alexander Garagatyi
 * @author Valeriy Svydenko
 */
@Singleton
public class CheAccessTokenCredentialProvider implements CredentialsProvider {

    private static String OAUTH_PROVIDER_NAME = "che";
    private final String        cheHostName;


    @Inject
    public CheAccessTokenCredentialProvider(@Named("api.endpoint") String apiEndPoint) throws URISyntaxException {
        this.cheHostName = new URI(apiEndPoint).getHost();
    }

    @Override
    public UserCredential getUserCredential() throws GitException {
        String token = EnvironmentContext.getCurrent()
                                         .getSubject()
                                         .getToken();
        if (token != null) {
            return new UserCredential(token, "x-che", OAUTH_PROVIDER_NAME);
        }
        return null;
    }

    @Override
    public String getId() {
        return OAUTH_PROVIDER_NAME;
    }

    @Override
    public boolean canProvideCredentials(String url) {
        return url.contains(cheHostName);
    }

    @Override
    public ProviderInfo getProviderInfo() {
        return null;
    }
}
