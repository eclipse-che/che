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

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.api.client.util.store.MemoryDataStoreFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.security.oauth.shared.User;

/**
 * OAuth authentication for OpenShift.
 *
 * @author Igor Vinokur
 */
@Singleton
public class OpenShiftOAuthAuthenticator extends OAuthAuthenticator {
  private final String openshiftEndpoint;

  @Inject
  public OpenShiftOAuthAuthenticator(
      @Nullable @Named("che.oauth.openshift.clientid") String clientId,
      @Nullable @Named("che.oauth.openshift.clientsecret") String clientSecret,
      @Nullable @Named("che.oauth.openshift.endpoint") String openshiftEndpoint,
      @Named("che.api") String apiEndpoint)
      throws IOException {
    openshiftEndpoint =
        openshiftEndpoint.endsWith("/") ? openshiftEndpoint : openshiftEndpoint + "/";
    this.openshiftEndpoint = openshiftEndpoint;
    String[] redirectUrl = {apiEndpoint + "/oauth/callback"};
    if (!isNullOrEmpty(clientId)
        && !isNullOrEmpty(clientSecret)
        && !isNullOrEmpty(openshiftEndpoint)) {
      configure(
          clientId,
          clientSecret,
          redirectUrl,
          openshiftEndpoint + "oauth/authorize",
          openshiftEndpoint + "oauth/token",
          new MemoryDataStoreFactory());
    }
  }

  @Override
  public User getUser(OAuthToken accessToken) throws OAuthAuthenticationException {
    throw new OAuthAuthenticationException("not supported");
  }

  @Override
  public final String getOAuthProvider() {
    return "openshift";
  }

  @Override
  public OAuthToken getToken(String userId) throws IOException {
    final OAuthToken token = super.getToken(userId);
    // Check if the token is valid for requests.
    if (!(token == null || token.getToken() == null || token.getToken().isEmpty())) {
      String tokenVerifyUrl = this.openshiftEndpoint + "oapi/v1/projects";
      HttpURLConnection http = null;
      try {
        http = (HttpURLConnection) new URL(tokenVerifyUrl).openConnection();
        http.setInstanceFollowRedirects(false);
        http.setRequestMethod("GET");
        http.setRequestProperty("Authorization", "Bearer " + token.getToken());
        http.setRequestProperty("Accept", "application/json");

        if (http.getResponseCode() == 401) {
          return null;
        }
      } finally {
        if (http != null) {
          http.disconnect();
        }
      }

      return token;
    }
    return null;
  }
}
