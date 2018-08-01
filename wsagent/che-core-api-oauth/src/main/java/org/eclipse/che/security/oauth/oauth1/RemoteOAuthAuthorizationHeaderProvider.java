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
package org.eclipse.che.security.oauth.oauth1;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.security.oauth.shared.OAuthAuthorizationHeaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allow get authorization header from OAuth1 service over http.
 *
 * @author Igor Vinokur
 */
public class RemoteOAuthAuthorizationHeaderProvider implements OAuthAuthorizationHeaderProvider {
  private static final Logger LOG =
      LoggerFactory.getLogger(RemoteOAuthAuthorizationHeaderProvider.class);

  private final String apiEndpoint;

  private final HttpJsonRequestFactory httpJsonRequestFactory;

  @Inject
  public RemoteOAuthAuthorizationHeaderProvider(
      @Named("che.api") String apiEndpoint, HttpJsonRequestFactory httpJsonRequestFactory) {
    this.apiEndpoint = apiEndpoint;
    this.httpJsonRequestFactory = httpJsonRequestFactory;
  }

  @Override
  public String getAuthorizationHeader(
      String oauthProviderName,
      String userId,
      String requestType,
      String requestUrl,
      Map<String, String> requestParameters) {
    if (oauthProviderName.isEmpty() || userId.isEmpty()) {
      return null;
    }
    UriBuilder uriBuilder =
        UriBuilder.fromUri(apiEndpoint)
            .path("/oauth/signature")
            .queryParam("user_id", userId)
            .queryParam("oauth_provider", oauthProviderName)
            .queryParam("request_method", requestType)
            .queryParam("request_url", requestUrl);
    try {
      return httpJsonRequestFactory
          .fromUrl(uriBuilder.build().toString())
          .useGetMethod()
          .request()
          .asString();
    } catch (Exception e) {
      LOG.warn(e.getLocalizedMessage());
      return null;
    }
  }
}
