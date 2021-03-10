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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class GitlabFileContentProviderTest {

  @Test
  public void shouldExpandRelativePaths() throws Exception {
    URLFetcher urlFetcher = Mockito.mock(URLFetcher.class);
    GitlabUrl gitlabUrl =
        new GitlabUrl()
            .withHostName("https://gitlab.net")
            .withUsername("eclipse")
            .withProject("che");
    FileContentProvider fileContentProvider = new GitlabFileContentProvider(gitlabUrl, urlFetcher);
    fileContentProvider.fetchContent("devfile.yaml");
    verify(urlFetcher).fetch(eq("https://gitlab.net/eclipse/che/-/raw/master/devfile.yaml"));
  }

  @Test
  public void shouldPreserveAbsolutePaths() throws Exception {
    URLFetcher urlFetcher = Mockito.mock(URLFetcher.class);
    GitlabUrl gitlabUrl =
        new GitlabUrl().withHostName("gitlab.net").withUsername("eclipse").withProject("che");
    FileContentProvider fileContentProvider = new GitlabFileContentProvider(gitlabUrl, urlFetcher);
    String url = "https://gitlab.net/eclipse/che/-/raw/master/devfile.yaml";
    fileContentProvider.fetchContent(url);
    verify(urlFetcher).fetch(eq(url));
  }
}
