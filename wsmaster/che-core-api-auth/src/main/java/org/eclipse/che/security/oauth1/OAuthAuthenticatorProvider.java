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
package org.eclipse.che.security.oauth1;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Allow store and provide services with implementations of {@link OAuthAuthenticator} for OAuth 1.
 *
 * @author Kevin Pollet
 * @author Igor Vinokur
 */
@Singleton
public class OAuthAuthenticatorProvider {
  private final Map<String, OAuthAuthenticator> oAuthAuthenticators = new HashMap<>();

  @Inject
  public OAuthAuthenticatorProvider(final Set<OAuthAuthenticator> oAuthAuthenticators) {
    oAuthAuthenticators.forEach(
        authenticator ->
            this.oAuthAuthenticators.put(authenticator.getOAuthProvider(), authenticator));
  }

  /**
   * Get the OAuth authentication service by name.
   *
   * @param oauthProviderName name of the OAuth provider.
   * @return {@link OAuthAuthenticator} instance or {@code null} if specified OAuth provider is not
   *     supported.
   */
  public OAuthAuthenticator getAuthenticator(String oauthProviderName) {
    return oAuthAuthenticators.get(oauthProviderName);
  }
}
