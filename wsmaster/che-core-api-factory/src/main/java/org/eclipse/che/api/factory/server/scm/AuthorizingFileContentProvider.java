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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmConfigurationPersistenceException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;
import org.eclipse.che.api.factory.server.scm.exception.UnsatisfiedScmPreconditionException;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Common implementation of file content provider which is able to access content of private
 * repositories using personal access tokens from specially formatted secret in user's namespace.
 */
public class AuthorizingFileContentProvider<T extends RemoteFactoryUrl>
    implements FileContentProvider {

  private final T remoteFactoryUrl;
  private final URLFetcher urlFetcher;
  private final PersonalAccessTokenManager personalAccessTokenManager;
  private final GitCredentialManager gitCredentialManager;

  public AuthorizingFileContentProvider(
      T remoteFactoryUrl,
      URLFetcher urlFetcher,
      PersonalAccessTokenManager personalAccessTokenManager,
      GitCredentialManager gitCredentialManager) {
    this.remoteFactoryUrl = remoteFactoryUrl;
    this.urlFetcher = urlFetcher;
    this.personalAccessTokenManager = personalAccessTokenManager;
    this.gitCredentialManager = gitCredentialManager;
  }

  @Override
  public String fetchContent(String fileURL) throws IOException, DevfileException {
    String requestURL;
    try {
      if (new URI(fileURL).isAbsolute()) {
        requestURL = fileURL;
      } else {
        // since files retrieved via REST, we cannot use path symbols like . ./ so cut them off
        requestURL = remoteFactoryUrl.rawFileLocation(fileURL.replaceAll("^[/.]+", ""));
      }
    } catch (URISyntaxException e) {
      throw new DevfileException(e.getMessage(), e);
    }
    try {
      Optional<PersonalAccessToken> token =
          personalAccessTokenManager.get(
              EnvironmentContext.getCurrent().getSubject(), remoteFactoryUrl.getHostName());
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
                    EnvironmentContext.getCurrent().getSubject(), remoteFactoryUrl.getHostName());
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
