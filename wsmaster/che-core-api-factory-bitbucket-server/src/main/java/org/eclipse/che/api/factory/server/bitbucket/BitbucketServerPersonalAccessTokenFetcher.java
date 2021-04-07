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
package org.eclipse.che.api.factory.server.bitbucket;

import static java.lang.String.format;
import static java.lang.String.valueOf;

import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.factory.server.bitbucket.server.BitbucketPersonalAccessToken;
import org.eclipse.che.api.factory.server.bitbucket.server.BitbucketServerApiClient;
import org.eclipse.che.api.factory.server.bitbucket.server.BitbucketUser;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.api.factory.server.scm.PersonalAccessTokenFetcher;
import org.eclipse.che.api.factory.server.scm.exception.ScmBadRequestException;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmItemNotFoundException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bitbucket implementation for {@link PersonalAccessTokenFetcher}. Right now returns {@code null}
 * for all possible SCM URL-s (which is valid value) but later will be extended to fully featured
 * class.
 */
public class BitbucketServerPersonalAccessTokenFetcher implements PersonalAccessTokenFetcher {

  private static final Logger LOG =
      LoggerFactory.getLogger(BitbucketServerPersonalAccessTokenFetcher.class);

  private static final String TOKEN_NAME_TEMPLATE = "che-token-<%s>-<%s>";
  private final BitbucketServerApiClient bitbucketServerApiClient;
  private final URL apiEndpoint;

  @Inject
  public BitbucketServerPersonalAccessTokenFetcher(
      BitbucketServerApiClient bitbucketServerApiClient, @Named("che.api") URL apiEndpoint) {
    this.bitbucketServerApiClient = bitbucketServerApiClient;
    this.apiEndpoint = apiEndpoint;
  }

  @Override
  public PersonalAccessToken fetchPersonalAccessToken(Subject cheUser, String scmServerUrl)
      throws ScmUnauthorizedException, ScmCommunicationException {
    if (!bitbucketServerApiClient.isConnected(scmServerUrl)) {
      LOG.debug("not a  valid url {} for current fetcher ", scmServerUrl);
      return null;
    }

    final String tokenName =
        format(TOKEN_NAME_TEMPLATE, cheUser.getUserId(), apiEndpoint.getHost());
    try {
      BitbucketUser user =
          bitbucketServerApiClient.getUser(EnvironmentContext.getCurrent().getSubject());
      LOG.debug("Current bitbucket user {} ", user);
      // cleanup existed
      List<BitbucketPersonalAccessToken> existingTokens =
          bitbucketServerApiClient
              .getPersonalAccessTokens(user.getSlug())
              .stream()
              .filter(p -> p.getName().equals(tokenName))
              .collect(Collectors.toList());
      for (BitbucketPersonalAccessToken existedToken : existingTokens) {
        LOG.debug("Deleting existed che token {} {}", existedToken.getId(), existedToken.getName());
        bitbucketServerApiClient.deletePersonalAccessTokens(user.getSlug(), existedToken.getId());
      }

      BitbucketPersonalAccessToken token =
          bitbucketServerApiClient.createPersonalAccessTokens(
              user.getSlug(), tokenName, ImmutableSet.of("PROJECT_WRITE", "REPO_WRITE"));
      LOG.debug("Token created = {} for {}", token.getId(), token.getUser());
      return new PersonalAccessToken(
          scmServerUrl,
          EnvironmentContext.getCurrent().getSubject().getUserId(),
          user.getName(),
          valueOf(user.getId()),
          token.getName(),
          valueOf(token.getId()),
          token.getToken());
    } catch (ScmBadRequestException | ScmItemNotFoundException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }

  @Override
  public Optional<Boolean> isValid(PersonalAccessToken personalAccessToken)
      throws ScmCommunicationException, ScmItemNotFoundException {
    LOG.info("Checking personalAccessToken {}", personalAccessToken);
    if (!bitbucketServerApiClient.isConnected(personalAccessToken.getScmProviderUrl())) {
      LOG.debug(
          "not a  valid url {} for current fetcher ", personalAccessToken.getScmProviderUrl());
      return Optional.empty();
    }
    return Optional.of(
        bitbucketServerApiClient.isValidPersonalAccessToken(
            personalAccessToken.getScmUserName(), personalAccessToken.getToken()));
  }
}
