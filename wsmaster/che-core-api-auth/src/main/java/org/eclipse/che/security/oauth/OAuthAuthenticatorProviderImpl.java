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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Singleton
public class OAuthAuthenticatorProviderImpl implements OAuthAuthenticatorProvider {
    private final Map<String, OAuthAuthenticator> authenticatorMap = new HashMap<>();

    @Inject
    public OAuthAuthenticatorProviderImpl(Set<OAuthAuthenticator> oAuthAuthenticators) {
        for (OAuthAuthenticator authenticator : oAuthAuthenticators) {
            if (authenticator.isConfigured()) {
                authenticatorMap.put(authenticator.getOAuthProvider(), authenticator);
            }
        }
    }

    @Override
    public OAuthAuthenticator getAuthenticator(String oauthProviderName) {
        return authenticatorMap.get(oauthProviderName);
    }

    @Override
    public Set<String> getRegisteredProviderNames() {
        return Collections.unmodifiableSet(authenticatorMap.keySet());
    }
}
