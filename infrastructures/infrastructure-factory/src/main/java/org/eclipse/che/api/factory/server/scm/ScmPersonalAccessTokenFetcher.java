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

import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;

/**
 * Iterate over configured list of PersonalAccessTokenFetcher with attempt to get
 * PersonalAccessToken.
 */
public class ScmPersonalAccessTokenFetcher {
  private Set<PersonalAccessTokenFetcher> personalAccessTokenFetchers;

  @Inject
  public ScmPersonalAccessTokenFetcher(
      Set<PersonalAccessTokenFetcher> personalAccessTokenFetchers) {
    this.personalAccessTokenFetchers = personalAccessTokenFetchers;
  }

  public PersonalAccessToken fetchPersonalAccessToken(String cheUserId, String scmServerUrl)
      throws ScmUnauthorizedException, ScmCommunicationException, UnknownScmProviderException {
    for (PersonalAccessTokenFetcher fetcher : personalAccessTokenFetchers) {
      PersonalAccessToken token = fetcher.fetchPersonalAccessToken(cheUserId, scmServerUrl);
      if (token != null) {
        return token;
      }
    }
    throw new UnknownScmProviderException(
        "No PersonalAccessTokenFetcher configured for " + scmServerUrl, scmServerUrl);
  }
}
