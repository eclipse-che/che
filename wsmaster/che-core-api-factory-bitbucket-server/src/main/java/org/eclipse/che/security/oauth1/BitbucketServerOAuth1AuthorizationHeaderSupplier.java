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

import com.google.common.base.Strings;
import javax.inject.Inject;
import org.eclipse.che.api.factory.server.bitbucket.server.AuthorizationHeaderSupplier;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

/**
 * Implementation of @{@link AuthorizationHeaderSupplier} that is used @{@link
 * BitbucketServerOAuthAuthenticator} to compute authorization headers.
 */
public class BitbucketServerOAuth1AuthorizationHeaderSupplier
    implements AuthorizationHeaderSupplier {
  private final BitbucketServerOAuthAuthenticator authenticator;

  @Inject
  public BitbucketServerOAuth1AuthorizationHeaderSupplier(
      BitbucketServerOAuthAuthenticator authenticator) {
    this.authenticator = authenticator;
  }

  @Override
  public String computeAuthorizationHeader(String requestMethod, String requestUrl)
      throws ScmUnauthorizedException, ScmCommunicationException {
    try {
      Subject subject = EnvironmentContext.getCurrent().getSubject();
      String authorizationHeader =
          authenticator.computeAuthorizationHeader(subject.getUserId(), requestMethod, requestUrl);
      if (Strings.isNullOrEmpty(authorizationHeader)) {
        throw new ScmUnauthorizedException(
            subject.getUserName()
                + " is not authorized in "
                + authenticator.getOAuthProvider()
                + " OAuth1 provider",
            authenticator.getOAuthProvider(),
            "1.0",
            authenticator.getLocalAuthenticateUrl());
      }
      return authorizationHeader;
    } catch (OAuthAuthenticationException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }
}
