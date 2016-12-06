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
package org.eclipse.che.ide.oauth;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.util.loging.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Map based implementation of OAuth registry
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class OAuth2AuthenticatorRegistryImpl implements OAuth2AuthenticatorRegistry {

    private final Map<String, OAuth2Authenticator> authenticators;

    @Inject
    public OAuth2AuthenticatorRegistryImpl(Set<OAuth2Authenticator> oAuth2Authenticators) {
        authenticators = new HashMap<>(oAuth2Authenticators.size());
        for (OAuth2Authenticator authenticator : oAuth2Authenticators) {
            final String providerName = authenticator.getProviderName();
            if (authenticators.get(providerName) != null) {
                Log.warn(OAuth2AuthenticatorRegistryImpl.class, "OAuth2Authenticator provider " + providerName + " already registered. But can be only one");
            } else {
                registerAuthenticator(providerName, authenticator);
            }
        }
    }

    @Override
    public void registerAuthenticator(String providerName, OAuth2Authenticator oAuth2Authenticator) {
        authenticators.put(providerName, oAuth2Authenticator);
    }

    @Override
    public OAuth2Authenticator getAuthenticator(String providerName) {
        return authenticators.get(providerName);
    }
}
