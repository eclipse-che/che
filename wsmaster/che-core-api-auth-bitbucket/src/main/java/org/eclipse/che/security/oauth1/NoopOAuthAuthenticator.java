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

import java.net.URL;

/**
 * Dummy implementation of @{@link OAuthAuthenticator} used in the case if no Bitbucket Server
 * integration is configured.
 */
public class NoopOAuthAuthenticator extends OAuthAuthenticator {
  protected NoopOAuthAuthenticator() {
    super(null, null, null, null, null, null, null);
  }

  @Override
  String getOAuthProvider() {
    return "Noop";
  }

  @Override
  String getAuthenticateUrl(URL requestUrl, String requestMethod, String signatureMethod)
      throws OAuthAuthenticationException {
    throw new RuntimeException(
        "The fallback noop authenticator cannot be used for authentication. Make sure OAuth is properly configured.");
  }

  @Override
  String callback(URL requestUrl) throws OAuthAuthenticationException {
    throw new RuntimeException(
        "The fallback noop authenticator cannot be used for authentication. Make sure OAuth is properly configured.");
  }

  @Override
  String computeAuthorizationHeader(String userId, String requestMethod, String requestUrl)
      throws OAuthAuthenticationException {
    throw new RuntimeException(
        "The fallback noop authenticator cannot be used for authentication. Make sure OAuth is properly configured.");
  }

  @Override
  public String getLocalAuthenticateUrl() {
    throw new RuntimeException(
        "The fallback noop authenticator cannot be used for authentication. Make sure OAuth is properly configured.");
  }
}
