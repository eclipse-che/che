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

import java.util.Optional;
import org.eclipse.che.api.factory.server.scm.AuthorizingFileContentProvider;
import org.eclipse.che.api.factory.server.scm.GitCredentialManager;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.api.factory.server.scm.PersonalAccessTokenManager;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmConfigurationPersistenceException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;
import org.eclipse.che.api.factory.server.scm.exception.UnsatisfiedScmPreconditionException;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Bitbucket specific file content provider. Files are retrieved using bitbucket REST API and
 * personal access token based authentication is performed during requests.
 */
public class BitbucketServerAuthorizingFileContentProvider
    extends AuthorizingFileContentProvider<BitbucketUrl> {

  private final PersonalAccessTokenManager personalAccessTokenManager;

  public BitbucketServerAuthorizingFileContentProvider(
      BitbucketUrl bitbucketUrl,
      URLFetcher urlFetcher,
      GitCredentialManager gitCredentialManager,
      PersonalAccessTokenManager personalAccessTokenManager) {
    super(bitbucketUrl, urlFetcher, gitCredentialManager);
    this.personalAccessTokenManager = personalAccessTokenManager;
  }

  @Override
  protected PersonalAccessToken getScmAuthenticationToken(String requestURL)
      throws DevfileException {
    try {
      Optional<PersonalAccessToken> token =
          personalAccessTokenManager.get(
              EnvironmentContext.getCurrent().getSubject(), remoteFactoryUrl.getHostName());

      if (token.isPresent()) {
        return token.get();
      } else {
        try {
          return personalAccessTokenManager.fetchAndSave(
              EnvironmentContext.getCurrent().getSubject(), remoteFactoryUrl.getHostName());
        } catch (ScmUnauthorizedException
            | ScmCommunicationException
            | UnknownScmProviderException e) {
          throw new DevfileException(e.getMessage(), e);
        }
      }
    } catch (ScmConfigurationPersistenceException | UnsatisfiedScmPreconditionException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }
}
