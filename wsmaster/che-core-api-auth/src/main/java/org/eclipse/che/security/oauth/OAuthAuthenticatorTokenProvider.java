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
package org.eclipse.che.security.oauth;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/** Retrieves oAuth token with help of OAuthAuthenticatorProvider. */
@Singleton
public class OAuthAuthenticatorTokenProvider implements OAuthTokenProvider {
    private final OAuthAuthenticatorProvider oAuthAuthenticatorProvider;

    @Inject
    public OAuthAuthenticatorTokenProvider(OAuthAuthenticatorProvider oAuthAuthenticatorProvider) {
        this.oAuthAuthenticatorProvider = oAuthAuthenticatorProvider;
    }

    @Override
    public OAuthToken getToken(String oauthProviderName, String userId) throws IOException {
        OAuthAuthenticator oAuthAuthenticator = oAuthAuthenticatorProvider.getAuthenticator(oauthProviderName);
        OAuthToken token;
        if (oAuthAuthenticator != null && (token = oAuthAuthenticator.getToken(userId)) != null) {
            return token;
        }
        return null;
    }
}
