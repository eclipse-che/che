/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.scm;

import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;
import org.eclipse.che.commons.subject.Subject;

/**
 * Iterate over configured list of PersonalAccessTokenFetcher with attempt to get
 * PersonalAccessToken.
 */
public class ScmPersonalAccessTokenFetcher {
  private final Set<PersonalAccessTokenFetcher> personalAccessTokenFetchers;

  @Inject
  public ScmPersonalAccessTokenFetcher(
      Set<PersonalAccessTokenFetcher> personalAccessTokenFetchers) {
    this.personalAccessTokenFetchers = personalAccessTokenFetchers;
  }

  /**
   * Iterate over the Set<PersonalAccessTokenFetcher> declared in container and sequentially invoke
   * {@link PersonalAccessTokenFetcher#fetchPersonalAccessToken(Subject, String)} method.
   *
   * @throws UnknownScmProviderException - if none of PersonalAccessTokenFetchers return a
   *     meaningful result.
   */
  public PersonalAccessToken fetchPersonalAccessToken(Subject cheUser, String scmServerUrl)
      throws ScmUnauthorizedException, ScmCommunicationException, UnknownScmProviderException {
    for (PersonalAccessTokenFetcher fetcher : personalAccessTokenFetchers) {
      PersonalAccessToken token = fetcher.fetchPersonalAccessToken(cheUser, scmServerUrl);
      if (token != null) {
        return token;
      }
    }
    throw new UnknownScmProviderException(
        "No PersonalAccessTokenFetcher configured for " + scmServerUrl, scmServerUrl);
  }

  /**
   * Iterate over the Set<PersonalAccessTokenFetcher> declared in container and sequentially invoke
   * {@link PersonalAccessTokenFetcher#isValid(PersonalAccessToken)} method.
   *
   * @throws UnknownScmProviderException - if none of PersonalAccessTokenFetchers return a
   *     meaningful result.
   */
  public boolean isValid(PersonalAccessToken personalAccessToken)
      throws UnknownScmProviderException, ScmUnauthorizedException, ScmCommunicationException {
    for (PersonalAccessTokenFetcher fetcher : personalAccessTokenFetchers) {

      Optional<Boolean> isValid = fetcher.isValid(personalAccessToken);
      if (isValid.isPresent()) {
        return isValid.get();
      }
    }
    throw new UnknownScmProviderException(
        "No PersonalAccessTokenFetcher configured for " + personalAccessToken.getScmProviderUrl(),
        personalAccessToken.getScmProviderUrl());
  }
}
