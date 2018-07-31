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
package org.eclipse.che.security.oauth.shared;

import java.util.Map;

/**
 * Provides the Authorization header value from the OAuth 1 providers.
 *
 * @author Kevin Pollet
 * @author Igor Vinokur
 */
public interface OAuthAuthorizationHeaderProvider {

  /**
   * Returns the Authorization header value used to sign the request with OAuth.
   *
   * @param oauthProviderName the OAuth 1 provider name.
   * @param userId the user id.
   * @param requestMethod the HTTP request method.
   * @param requestUrl the HTTP request url with encoded query parameters.
   * @param requestParameters the HTTP request parameters. HTTP request parameters must include raw
   *     values of application/x-www-form-urlencoded POST parameters.
   * @return the Authorization header value or {@code null} if it cannot be computed.
   */
  String getAuthorizationHeader(
      String oauthProviderName,
      String userId,
      String requestMethod,
      String requestUrl,
      Map<String, String> requestParameters);
}
