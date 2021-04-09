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

/** Bitbucket Server API client. */
public interface BitbucketServerApiClient {
  /**
   * @param bitbucketServerUrl
   * @return - true if client is connected to the given bitbucket server.
   */
  boolean isConnected(String bitbucketServerUrl);
  /**
   * @param cheUser - Che user.
   * @return - {@link BitbucketUser} that is linked with given {@link Subject}
   * @throws ScmUnauthorizedException - in case if {@link Subject} is not linked to any {@link
   *     BitbucketUser}
   */
  BitbucketUser getUser(Subject cheUser)
      throws ScmUnauthorizedException, ScmCommunicationException, ScmItemNotFoundException;

  /**
   * @param slug
   * @return - Retrieve the {@link BitbucketUser} matching the supplied userSlug.
   * @throws ScmItemNotFoundException
   * @throws ScmUnauthorizedException
   * @throws ScmCommunicationException
   */
  BitbucketUser getUser(String slug)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException;

  /**
   * @return Retrieve a list of {@link BitbucketUser}. Only authenticated users may call this
   *     resource.
   * @throws ScmBadRequestException
   * @throws ScmUnauthorizedException
   * @throws ScmCommunicationException
   */
  List<BitbucketUser> getUsers()
      throws ScmBadRequestException, ScmUnauthorizedException, ScmCommunicationException;

  /**
   * @return Retrieve a list of {@link BitbucketUser}, optionally run through provided filters. Only
   *     authenticated users may call this resource.
   * @throws ScmBadRequestException
   * @throws ScmUnauthorizedException
   * @throws ScmCommunicationException
   */
  List<BitbucketUser> getUsers(String filter)
      throws ScmBadRequestException, ScmUnauthorizedException, ScmCommunicationException;

  /**
   * Modify an access token for the user according to the given request. Any fields not specified
   * will not be altered
   *
   * @param userSlug
   * @param tokenId - the token id
   * @throws ScmItemNotFoundException
   * @throws ScmUnauthorizedException
   * @throws ScmCommunicationException
   */
  void deletePersonalAccessTokens(String userSlug, Long tokenId)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException;

  /**
   * Create an access token for the user according to the given request.
   *
   * @param userSlug
   * @param tokenName
   * @param permissions
   * @return
   * @throws ScmBadRequestException
   * @throws ScmUnauthorizedException
   * @throws ScmCommunicationException
   */
  BitbucketPersonalAccessToken createPersonalAccessTokens(
      String userSlug, String tokenName, Set<String> permissions)
      throws ScmBadRequestException, ScmUnauthorizedException, ScmCommunicationException;

  /**
   * Get all personal access tokens associated with the given user
   *
   * @param userSlug
   * @return
   * @throws ScmItemNotFoundException
   * @throws ScmUnauthorizedException
   * @throws ScmBadRequestException
   * @throws ScmCommunicationException
   */
  List<BitbucketPersonalAccessToken> getPersonalAccessTokens(String userSlug)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException;

  /**
   * @param userSlug - user's slug.
   * @param tokenId - bitbucket personal access token id.
   * @return - Bitbucket personal access token.
   * @throws ScmCommunicationException
   */
  BitbucketPersonalAccessToken getPersonalAccessToken(String userSlug, Long tokenId)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException;
}
