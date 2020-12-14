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

  private final String userInfoEndpoint;
  private final String tokenEndpoint;
  private final String jwksUri;
  private final String endSessionEndpoint;
  private final String authServerURL;

  public OIDCInfo(
      String userInfoEndpoint,
      String tokenEndpoint,
      String jwksUri,
      String endSessionEndpoint,
      String authServerURL) {
    this.userInfoEndpoint = userInfoEndpoint;
    this.tokenEndpoint = tokenEndpoint;
    this.jwksUri = jwksUri;
    this.endSessionEndpoint = endSessionEndpoint;
    this.authServerURL = authServerURL;
  }

  /** @return url to get user profile information. */
  public String getUserInfoEndpoint() {
    return userInfoEndpoint;
  }

  /** @return url to retrieve token */
  public String getTokenEndpoint() {
    return tokenEndpoint;
  }

  /** @return url to retrieve JWK public key for token validation. */
  public String getJwksUri() {
    return jwksUri;
  }

  /** @return log out url. */
  public String getEndSessionEndpoint() {
    return endSessionEndpoint;
  }

  /** @return OIDC auth endpoint url */
  public String getAuthServerURL() {
    return authServerURL;
  }
}
