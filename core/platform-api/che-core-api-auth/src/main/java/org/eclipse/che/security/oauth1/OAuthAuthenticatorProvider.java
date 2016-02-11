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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Allow store and provide services which implementations of {@link OAuthAuthenticator} for OAuth 1.
 *
 * @author Kevin Pollet
 */
@Singleton
public class OAuthAuthenticatorProvider {
    private final Map<String, OAuthAuthenticator> oAuthAuthenticators = new HashMap<>();

    @Inject
    public OAuthAuthenticatorProvider(final Set<OAuthAuthenticator> oAuthAuthenticators) {
        for (final OAuthAuthenticator oneOAuthAuthenticator : oAuthAuthenticators) {
            this.oAuthAuthenticators.put(oneOAuthAuthenticator.getOAuthProvider(), oneOAuthAuthenticator);
        }
    }

    /**
     * Get the OAuth authentication service by name.
     *
     * @param oauthProviderName
     *         name of OAuth provider.
     * @return {@link OAuthAuthenticator} instance or {@code null} if specified OAuth provider is not supported.
     */
    public OAuthAuthenticator getAuthenticator(String oauthProviderName) {
        return oAuthAuthenticators.get(oauthProviderName);
    }
}
