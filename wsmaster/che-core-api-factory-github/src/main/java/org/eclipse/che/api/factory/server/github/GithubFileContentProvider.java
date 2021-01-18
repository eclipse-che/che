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
package org.eclipse.che.api.factory.server.github;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;

/** Github specific file content provider. */
class GithubFileContentProvider implements FileContentProvider {

  private final GithubUrl githubUrl;
  private final URLFetcher urlFetcher;

  GithubFileContentProvider(GithubUrl githubUrl, URLFetcher urlFetcher) {
    this.githubUrl = githubUrl;
    this.urlFetcher = urlFetcher;
  }

  @Override
  public String fetchContent(String fileURL) throws IOException, DevfileException {
    String requestURL;
    try {
      if (new URI(fileURL).isAbsolute()) {
        requestURL = fileURL;
      } else {
        requestURL = githubUrl.rawFileLocation(fileURL);
      }
    } catch (URISyntaxException e) {
      throw new DevfileException(e.getMessage(), e);
    }
    return urlFetcher.fetch(requestURL);
  }
}
