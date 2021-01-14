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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.eclipse.che.api.factory.server.scm.GitCredentialManager;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.api.factory.server.scm.PersonalAccessTokenManager;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmConfigurationPersistenceException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;
import org.eclipse.che.api.factory.server.scm.exception.UnsatisfiedScmPreconditionException;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Bitbucket specific file content provider. Files are retrieved using bitbucket REST API and
 * personal access token based authentication is performed during requests.
 */
public class BitbucketServerAuthorizingFileContentProvider implements FileContentProvider {

  private final URLFetcher urlFetcher;
  private final BitbucketUrl bitbucketUrl;
  private final GitCredentialManager gitCredentialManager;
  private final PersonalAccessTokenManager personalAccessTokenManager;

  public BitbucketServerAuthorizingFileContentProvider(
      BitbucketUrl bitbucketUrl,
      URLFetcher urlFetcher,
      GitCredentialManager gitCredentialManager,
      PersonalAccessTokenManager personalAccessTokenManager) {
    this.bitbucketUrl = bitbucketUrl;
    this.urlFetcher = urlFetcher;
    this.gitCredentialManager = gitCredentialManager;
    this.personalAccessTokenManager = personalAccessTokenManager;
  }

  @Override
  public String fetchContent(String fileURL) throws IOException, DevfileException {
    String requestURL;
    try {
      if (new URI(fileURL).isAbsolute()) {
        requestURL = fileURL;
      } else {
        // since files retrieved via REST, we cannot use path symbols like . ./ so cut them off
        requestURL = bitbucketUrl.rawFileLocation(fileURL.replaceAll("^[/.]+", ""));
      }
    } catch (URISyntaxException e) {
      throw new DevfileException(e.getMessage(), e);
    }
    try {
      Optional<PersonalAccessToken> token =
          personalAccessTokenManager.get(
              EnvironmentContext.getCurrent().getSubject(), bitbucketUrl.getHostName());
      if (token.isPresent()) {
        PersonalAccessToken personalAccessToken = token.get();
        String content = urlFetcher.fetch(requestURL, "Bearer " + personalAccessToken.getToken());
        gitCredentialManager.createOrReplace(personalAccessToken);
        return content;
      } else {
        try {
          return urlFetcher.fetch(requestURL);
        } catch (IOException exception) {
          // unable to determine exact cause, so let's just try to authorize...
          try {
            PersonalAccessToken personalAccessToken =
                personalAccessTokenManager.fetchAndSave(
                    EnvironmentContext.getCurrent().getSubject(), bitbucketUrl.getHostName());
            String content =
                urlFetcher.fetch(requestURL, "Bearer " + personalAccessToken.getToken());
            gitCredentialManager.createOrReplace(personalAccessToken);
            return content;
          } catch (ScmUnauthorizedException
              | ScmCommunicationException
              | UnknownScmProviderException e) {
            throw new DevfileException(e.getMessage(), e);
          }
        }
      }

    } catch (IOException e) {
      throw new IOException(
          String.format(
              "Failed to fetch a content from URL %s. Make sure the URL"
                  + " is correct. Additionally, if you're using "
                  + " relative form, make sure the referenced files are actually stored"
                  + " relative to the devfile on the same host,"
                  + " or try to specify URL in absolute form. The current attempt to download"
                  + " the file failed with the following error message: %s",
              fileURL, e.getMessage()),
          e);
    } catch (ScmConfigurationPersistenceException | UnsatisfiedScmPreconditionException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }
}
