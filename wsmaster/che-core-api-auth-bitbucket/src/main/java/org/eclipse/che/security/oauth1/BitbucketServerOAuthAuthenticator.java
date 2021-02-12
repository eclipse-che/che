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

import com.google.inject.Singleton;

/**
 * OAuth1 authentication for Bitbucket Server account.
 *
 * @author Igor Vinokur
 */
@Singleton
public class BitbucketServerOAuthAuthenticator extends OAuthAuthenticator {
  public static final String AUTHENTICATOR_NAME = "bitbucket-server";

  public BitbucketServerOAuthAuthenticator(
      String consumerKey, String privateKey, String bitbucketEndpoint, String apiEndpoint) {
    super(
        consumerKey,
        bitbucketEndpoint + "/plugins/servlet/oauth/request-token",
        bitbucketEndpoint + "/plugins/servlet/oauth/access-token",
        bitbucketEndpoint + "/plugins/servlet/oauth/authorize",
        apiEndpoint + "/oauth/1.0/callback",
        null,
        privateKey);
  }

  @Override
  public final String getOAuthProvider() {
    return AUTHENTICATOR_NAME;
  }
}
