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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class GithubFileContentProviderTest {

  @Test
  public void shouldExpandRelativePaths() throws Exception {
    URLFetcher urlFetcher = Mockito.mock(URLFetcher.class);
    GithubUrl githubUrl = new GithubUrl().withUsername("eclipse").withRepository("che");
    FileContentProvider fileContentProvider = new GithubFileContentProvider(githubUrl, urlFetcher);
    fileContentProvider.fetchContent("devfile.yaml");
    verify(urlFetcher).fetch(eq("https://raw.githubusercontent.com/eclipse/che/HEAD/devfile.yaml"));
  }

  @Test
  public void shouldPreserveAbsolutePaths() throws Exception {
    URLFetcher urlFetcher = Mockito.mock(URLFetcher.class);
    GithubUrl githubUrl = new GithubUrl().withUsername("eclipse").withRepository("che");
    FileContentProvider fileContentProvider = new GithubFileContentProvider(githubUrl, urlFetcher);
    String url = "https://raw.githubusercontent.com/foo/bar/devfile.yaml";
    fileContentProvider.fetchContent(url);
    verify(urlFetcher).fetch(eq(url));
  }
}
