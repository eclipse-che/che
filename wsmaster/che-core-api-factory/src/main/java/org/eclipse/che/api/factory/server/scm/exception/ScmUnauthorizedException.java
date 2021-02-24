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
package org.eclipse.che.api.factory.server.scm.exception;

/** In case if OAuth1 or Oauth2 token is missing and we cant make any authorised calls */
public class ScmUnauthorizedException extends Exception {

  private final String oauthProvider;
  private final String oauthVersion;
  private final String authenticateUrl;

  public ScmUnauthorizedException(
      String message, String oauthProvider, String oauthVersion, String authenticateUrl) {
    super(message);
    this.oauthProvider = oauthProvider;
    this.oauthVersion = oauthVersion;
    this.authenticateUrl = authenticateUrl;
  }

  public ScmUnauthorizedException(
      String message,
      String oauthProvider,
      Throwable cause,
      String oauthVersion,
      String authenticateUrl) {
    super(message, cause);
    this.oauthProvider = oauthProvider;
    this.oauthVersion = oauthVersion;
    this.authenticateUrl = authenticateUrl;
  }

  public String getOauthProvider() {
    return oauthProvider;
  }

  public String getAuthenticateUrl() {
    return authenticateUrl;
  }

  public String getOauthVersion() {
    return oauthVersion;
  }
}
