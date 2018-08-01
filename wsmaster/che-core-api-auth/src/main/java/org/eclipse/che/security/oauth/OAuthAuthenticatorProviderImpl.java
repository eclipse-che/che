/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security.oauth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

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
