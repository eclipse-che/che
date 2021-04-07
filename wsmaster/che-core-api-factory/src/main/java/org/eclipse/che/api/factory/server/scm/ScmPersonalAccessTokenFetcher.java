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
package org.eclipse.che.api.factory.server.scm;

import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmItemNotFoundException;
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

  public boolean isValid(PersonalAccessToken personalAccessToken)
      throws UnknownScmProviderException {
    for (PersonalAccessTokenFetcher fetcher : personalAccessTokenFetchers) {
      try {
        Optional<Boolean> isValid = fetcher.isValid(personalAccessToken);
        if (isValid.isPresent()) {
          return isValid.get();
        }
      } catch (ScmCommunicationException e) {
        e.printStackTrace();
      } catch (ScmUnauthorizedException e) {
        e.printStackTrace();
      } catch (ScmItemNotFoundException e) {
        e.printStackTrace();
      }
    }
    throw new UnknownScmProviderException(
        "No PersonalAccessTokenFetcher configured for " + personalAccessToken.getScmProviderUrl(),
        personalAccessToken.getScmProviderUrl());
  }
}
