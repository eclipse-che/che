/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.security.oauth.shared.OAuthTokenProvider;

/** Retrieves OAuth token with help of OAuthAuthenticatorProvider. */
@Singleton
public class OAuthAuthenticatorTokenProvider implements OAuthTokenProvider {
  private final OAuthAuthenticatorProvider oAuthAuthenticatorProvider;

  @Inject
  public OAuthAuthenticatorTokenProvider(OAuthAuthenticatorProvider oAuthAuthenticatorProvider) {
    this.oAuthAuthenticatorProvider = oAuthAuthenticatorProvider;
  }

  @Override
  public OAuthToken getToken(String oauthProviderName, String userId) throws IOException {
    OAuthAuthenticator oAuthAuthenticator =
        oAuthAuthenticatorProvider.getAuthenticator(oauthProviderName);
    OAuthToken token;
    if (oAuthAuthenticator != null && (token = oAuthAuthenticator.getToken(userId)) != null) {
      return token;
    }
    return null;
  }
}
