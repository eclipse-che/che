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
package org.eclipse.che.security.oauth1;


import org.eclipse.che.api.auth.oauth.OAuthAuthorizationHeaderProvider;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Map;

/**
 * Compute the Authorization header used to sign the OAuth1 request with the help of an {@link OAuthAuthenticatorProvider}.
 *
 * @author Kevin Pollet
 */
@Singleton
public class OAuthAuthenticatorAuthorizationHeaderProvider implements OAuthAuthorizationHeaderProvider {
    private final OAuthAuthenticatorProvider oAuthAuthenticatorProvider;

    @Inject
    public OAuthAuthenticatorAuthorizationHeaderProvider(@NotNull final OAuthAuthenticatorProvider oAuthAuthenticatorProvider) {
        this.oAuthAuthenticatorProvider = oAuthAuthenticatorProvider;
    }

    @Override
    public String getAuthorizationHeader(@NotNull final String oauthProviderName,
                                         @NotNull final String userId,
                                         @NotNull final String requestMethod,
                                         @NotNull final String requestUrl,
                                         @NotNull final Map<String, String> requestParameters) throws IOException {

        final OAuthAuthenticator oAuthAuthenticator = oAuthAuthenticatorProvider.getAuthenticator(oauthProviderName);
        if (oAuthAuthenticator != null) {
            return oAuthAuthenticator.computeAuthorizationHeader(userId, requestMethod, requestUrl, requestParameters);
        }
        return null;
    }
}
