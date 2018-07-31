/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.token.provider.oauth;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.security.oauth.GitHubOAuthAuthenticator;

@Singleton
public class OpenShiftGitHubOAuthAuthenticator extends GitHubOAuthAuthenticator {

  @Inject
  public OpenShiftGitHubOAuthAuthenticator(
      @Nullable @Named("che.oauth.github.redirecturis") String[] redirectUris,
      @Nullable @Named("che.oauth.github.authuri") String authUri,
      @Nullable @Named("che.oauth.github.tokenuri") String tokenUri)
      throws IOException {

    super("NULL", "NULL", redirectUris, authUri, tokenUri);

    if (!isNullOrEmpty(authUri)
        && !isNullOrEmpty(tokenUri)
        && redirectUris != null
        && redirectUris.length != 0) {

      configure("NULL", "NULL", redirectUris, authUri, tokenUri, new MemoryDataStoreFactory());
    }
  }

  public void setToken(String userId, OAuthToken token) throws IOException {
    flow.createAndStoreCredential(
        new TokenResponse().setAccessToken(token.getToken()).setScope(token.getScope()), userId);
  }
}
