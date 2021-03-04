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
package org.eclipse.che.api.factory.server.gitlab;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;

/** Github specific file content provider. */
class GitlabFileContentProvider implements FileContentProvider {

  private final GitlabUrl gitlabUrl;
  private final URLFetcher urlFetcher;

  GitlabFileContentProvider(GitlabUrl githubUrl, URLFetcher urlFetcher) {
    this.gitlabUrl = githubUrl;
    this.urlFetcher = urlFetcher;
  }

  @Override
  public String fetchContent(String fileURL) throws IOException, DevfileException {
    String requestURL;
    try {
      if (new URI(fileURL).isAbsolute()) {
        requestURL = fileURL;
      } else {
        requestURL = gitlabUrl.rawFileLocation(fileURL);
      }
    } catch (URISyntaxException e) {
      throw new DevfileException(e.getMessage(), e);
    }
    return urlFetcher.fetch(requestURL);
  }
}
