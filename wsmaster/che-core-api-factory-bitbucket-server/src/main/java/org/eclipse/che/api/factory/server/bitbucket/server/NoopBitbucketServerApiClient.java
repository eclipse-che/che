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
package org.eclipse.che.api.factory.server.bitbucket.server;

import java.util.List;
import java.util.Set;
import org.eclipse.che.api.factory.server.scm.exception.ScmBadRequestException;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmItemNotFoundException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.commons.subject.Subject;

/**
 * Implementation of @{@link BitbucketServerApiClient} that is going to be deployed in container in
 * case if no integration with Bitbucket server is needed.
 */
public class NoopBitbucketServerApiClient implements BitbucketServerApiClient {
  @Override
  public boolean isConnected(String bitbucketServerUrl) {
    return false;
  }

  @Override
  public BitbucketUser getUser(Subject cheUser)
      throws ScmUnauthorizedException, ScmCommunicationException {
    throw new RuntimeException(
        "The fallback noop api client cannot be used for real operation. Make sure Bitbucket OAuth1 is properly configured.");
  }

  @Override
  public BitbucketUser getUser(String slug)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException {
    throw new RuntimeException(
        "The fallback noop api client cannot be used for real operation. Make sure Bitbucket OAuth1 is properly configured.");
  }

  @Override
  public List<BitbucketUser> getUsers()
      throws ScmBadRequestException, ScmUnauthorizedException, ScmCommunicationException {
    throw new RuntimeException(
        "The fallback noop api client cannot be used for real operation. Make sure Bitbucket OAuth1 is properly configured.");
  }

  @Override
  public List<BitbucketUser> getUsers(String filter)
      throws ScmBadRequestException, ScmUnauthorizedException, ScmCommunicationException {
    throw new RuntimeException(
        "The fallback noop api client cannot be used for real operation. Make sure Bitbucket OAuth1 is properly configured.");
  }

  @Override
  public void deletePersonalAccessTokens(String userSlug, Long tokenId)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException {
    throw new RuntimeException(
        "The fallback noop api client cannot be used for real operation. Make sure Bitbucket OAuth1 is properly configured.");
  }

  @Override
  public BitbucketPersonalAccessToken createPersonalAccessTokens(
      String userSlug, String tokenName, Set<String> permissions)
      throws ScmBadRequestException, ScmUnauthorizedException, ScmCommunicationException {
    throw new RuntimeException("Invalid usage of BitbucketServerApi");
  }

  @Override
  public List<BitbucketPersonalAccessToken> getPersonalAccessTokens(String userSlug)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException {
    throw new RuntimeException("Invalid usage of BitbucketServerApi");
  }

  @Override
  public BitbucketPersonalAccessToken getPersonalAccessToken(String userSlug, Long tokenId)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException {
    return null;
  }
}
