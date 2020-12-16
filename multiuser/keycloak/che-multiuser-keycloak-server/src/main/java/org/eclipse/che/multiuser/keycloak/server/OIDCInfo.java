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

package org.eclipse.che.multiuser.keycloak.server;

/** OIDCInfo - POJO object to store information about Keycloak api. */
public class OIDCInfo {

  private final String tokenPublicEndpoint;
  private final String endSessionPublicEndpoint;
  private final String userInfoPublicEndpoint;
  private final String userInfoEndpoint;
  private final String jwksPublicUri;
  private final String jwksUri;
  private final String authServerURL;

  public OIDCInfo(
      String tokenPublicEndpoint,
      String endSessionPublicEndpoint,
      String userInfoPublicEndpoint,
      String userInfoEndpoint,
      String jwksPublicUri,
      String jwksUri,
      String authServerURL) {
    this.tokenPublicEndpoint = tokenPublicEndpoint;
    this.endSessionPublicEndpoint = endSessionPublicEndpoint;
    this.userInfoPublicEndpoint = userInfoPublicEndpoint;
    this.userInfoEndpoint = userInfoEndpoint;
    this.jwksPublicUri = jwksPublicUri;
    this.jwksUri = jwksUri;

    this.authServerURL = authServerURL;
  }

  /** @return public url to retrieve token */
  public String getTokenPublicEndpoint() {
    return tokenPublicEndpoint;
  }

  /** @return public log out url. */
  public String getEndSessionPublicEndpoint() {
    return endSessionPublicEndpoint;
  }

  /** @return public url to get user profile information. */
  public String getUserInfoPublicEndpoint() {
    return userInfoPublicEndpoint;
  }

  /**
   * @return url to get user profile information. Url will be internal if internal network enabled,
   *     otherwise url will be public.
   */
  public String getUserInfoEndpoint() {
    return userInfoEndpoint;
  }

  /** @return public url to retrieve JWK public key for token validation. */
  public String getJwksPublicUri() {
    return jwksPublicUri;
  }

  /**
   * @return url to retrieve JWK public key for token validation. Url will be internal if internal
   *     network enabled, otherwise url will be public.
   */
  public String getJwksUri() {
    return jwksUri;
  }

  /**
   * @return OIDC auth endpoint url. Url will be internal if internal network enabled, otherwise url
   *     will be public.
   */
  public String getAuthServerURL() {
    return authServerURL;
  }
}
