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
import org.eclipse.che.api.factory.server.scm.exception.UnsatisfiedPreconditionException;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.commons.env.EnvironmentContext;

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
    URI fileURI;
    String requestURL;
    try {
      fileURI = new URI(fileURL);
    } catch (URISyntaxException e) {
      throw new DevfileException(e.getMessage(), e);
    }

    if (fileURI.isAbsolute()) {
      requestURL = fileURL;
    } else {
      // check me
      try {
        requestURL = new URI(bitbucketUrl.rawFileLocation("test.file")).resolve(fileURI).toString();
      } catch (URISyntaxException e) {
        throw new DevfileException(e.getMessage(), e);
      }
    }
    try {
      Optional<PersonalAccessToken> token =
          personalAccessTokenManager.get(
              EnvironmentContext.getCurrent().getSubject().getUserId(), bitbucketUrl.getHostName());
      if (token.isPresent()) {
        PersonalAccessToken personalAccessToken = token.get();
        String content = urlFetcher.fetch(requestURL, "Bearer " + personalAccessToken.getToken());
        gitCredentialManager.createOrReplace(personalAccessToken);
        return content;
      } else {
        try {
          return urlFetcher.fetch(requestURL);
        } catch (Exception exception) {
          // UnauthorizedException
          try {
            PersonalAccessToken personalAccessToken =
                personalAccessTokenManager.fetchAndSave(
                    EnvironmentContext.getCurrent().getSubject().getUserId(),
                    bitbucketUrl.getHostName());
            String content =
                urlFetcher.fetch(requestURL, "Bearer " + personalAccessToken.getToken());
            gitCredentialManager.createOrReplace(personalAccessToken);
            return content;
          } catch (ScmUnauthorizedException e) {
            // TODO proper error handling
            throw new DevfileException(e.getMessage());
          } catch (ScmCommunicationException e) {
            // TODO proper error handling
            throw new DevfileException(e.getMessage());
          } catch (UnknownScmProviderException unknownScmProvider) {
            // TODO proper error handling
            throw new DevfileException(unknownScmProvider.getMessage());
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
    } catch (ScmConfigurationPersistenceException e) {
      // TODO proper error handling
      throw new DevfileException(e.getMessage());
    } catch (UnsatisfiedPreconditionException e) {
      // TODO proper error handling
      throw new DevfileException(e.getMessage());
    }
  }
}
