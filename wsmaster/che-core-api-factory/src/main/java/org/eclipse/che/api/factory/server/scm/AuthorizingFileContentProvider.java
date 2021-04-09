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
import org.eclipse.che.api.factory.server.scm.exception.ScmConfigurationPersistenceException;
import org.eclipse.che.api.factory.server.scm.exception.UnsatisfiedScmPreconditionException;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;

/**
 * Common implementation of file content provider which is able to access content of private
 * repositories using personal access tokens from specially formatted secret in user's namespace.
 */
public abstract class AuthorizingFileContentProvider<T extends RemoteFactoryUrl>
    implements FileContentProvider {

  protected final T remoteFactoryUrl;
  private final URLFetcher urlFetcher;
  private final GitCredentialManager gitCredentialManager;

  public AuthorizingFileContentProvider(
      T remoteFactoryUrl, URLFetcher urlFetcher, GitCredentialManager gitCredentialManager) {
    this.remoteFactoryUrl = remoteFactoryUrl;
    this.urlFetcher = urlFetcher;
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
      try {
        return urlFetcher.fetch(requestURL);
      } catch (IOException exception) {
        // unable to determine exact cause, so let's just try to authorize...
        PersonalAccessToken scmAuthenticationToken = getScmAuthenticationToken(requestURL);
        String content =
            urlFetcher.fetch(requestURL, "Bearer " + scmAuthenticationToken.getToken());
        gitCredentialManager.createOrReplace(scmAuthenticationToken);
        return content;
      }
    } catch (ScmConfigurationPersistenceException | UnsatisfiedScmPreconditionException e) {
      throw new DevfileException(e.getMessage(), e);
    }
  }

  protected abstract PersonalAccessToken getScmAuthenticationToken(String requestURL)
      throws DevfileException;
}
