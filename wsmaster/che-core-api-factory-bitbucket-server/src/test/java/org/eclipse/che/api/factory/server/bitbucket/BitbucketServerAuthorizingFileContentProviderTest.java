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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.eclipse.che.api.factory.server.scm.GitCredentialManager;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.api.factory.server.scm.PersonalAccessTokenManager;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.commons.subject.Subject;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class BitbucketServerAuthorizingFileContentProviderTest {

  public static final String TEST_HOSTNAME = "https://foo.bar";
  @Mock private URLFetcher urlFetcher;
  @Mock private GitCredentialManager gitCredentialManager;
  @Mock private PersonalAccessTokenManager personalAccessTokenManager;

  @Test
  public void shouldFetchContentWithTokenIfPresent() throws Exception {
    BitbucketUrl url = new BitbucketUrl().withHostName(TEST_HOSTNAME);
    BitbucketServerAuthorizingFileContentProvider fileContentProvider =
        new BitbucketServerAuthorizingFileContentProvider(
            url, urlFetcher, gitCredentialManager, personalAccessTokenManager);

    PersonalAccessToken token = new PersonalAccessToken(TEST_HOSTNAME, "user1", "token");
    when(personalAccessTokenManager.get(any(Subject.class), anyString()))
        .thenReturn(Optional.of(token));

    String fileURL = "https://foo.bar/scm/repo/.devfile";

    // when
    fileContentProvider.fetchContent(fileURL);

    // then
    verify(urlFetcher).fetch(eq(fileURL), eq("Bearer token"));
  }

  @Test
  public void shouldFetchTokenIfNotYetPresent() throws Exception {
    BitbucketUrl url = new BitbucketUrl().withHostName(TEST_HOSTNAME);
    BitbucketServerAuthorizingFileContentProvider fileContentProvider =
        new BitbucketServerAuthorizingFileContentProvider(
            url, urlFetcher, gitCredentialManager, personalAccessTokenManager);

    PersonalAccessToken token = new PersonalAccessToken(TEST_HOSTNAME, "user1", "token");
    when(personalAccessTokenManager.get(any(Subject.class), anyString()))
        .thenReturn(Optional.empty());
    when(personalAccessTokenManager.fetchAndSave(any(Subject.class), eq(TEST_HOSTNAME)))
        .thenReturn(token);
    when(urlFetcher.fetch(anyString())).thenThrow(new IOException("unauthorized"));

    String fileURL = "https://foo.bar/scm/repo/.devfile";

    // when
    fileContentProvider.fetchContent(fileURL);

    // then
    verify(personalAccessTokenManager).fetchAndSave(any(Subject.class), eq(TEST_HOSTNAME));
    verify(urlFetcher).fetch(eq(fileURL), eq("Bearer token"));
    verify(gitCredentialManager).createOrReplace(eq(token));
  }

  @Test(dataProvider = "relativePathsProvider")
  public void shouldResolveRelativePaths(String relative, String expected, String branch)
      throws Exception {
    BitbucketUrl url =
        new BitbucketUrl()
            .withHostName(TEST_HOSTNAME)
            .withProject("proj")
            .withRepository("repo")
            .withDevfileFilenames(Collections.singletonList(".devfile"));
    if (branch != null) {
      url.withBranch(branch);
    }
    BitbucketServerAuthorizingFileContentProvider fileContentProvider =
        new BitbucketServerAuthorizingFileContentProvider(
            url, urlFetcher, gitCredentialManager, personalAccessTokenManager);
    PersonalAccessToken token = new PersonalAccessToken(TEST_HOSTNAME, "user1", "token");
    when(personalAccessTokenManager.get(any(Subject.class), anyString()))
        .thenReturn(Optional.of(token));

    // when
    fileContentProvider.fetchContent(relative);

    // then
    verify(urlFetcher).fetch(eq(expected), eq("Bearer token"));
  }

  @DataProvider
  public static Object[][] relativePathsProvider() {
    return new Object[][] {
      {"./file.txt", "https://foo.bar/rest/api/1.0/projects/proj/repos/repo/raw/file.txt", null},
      {"../file.txt", "https://foo.bar/rest/api/1.0/projects/proj/repos/repo/raw/file.txt", null},
      {"/file.txt", "https://foo.bar/rest/api/1.0/projects/proj/repos/repo/raw/file.txt", null},
      {"file.txt", "https://foo.bar/rest/api/1.0/projects/proj/repos/repo/raw/file.txt", null},
      {
        "foo/file.txt",
        "https://foo.bar/rest/api/1.0/projects/proj/repos/repo/raw/foo/file.txt",
        null
      },
      {
        "file.txt",
        "https://foo.bar/rest/api/1.0/projects/proj/repos/repo/raw/file.txt?at=foo",
        "foo"
      }
    };
  }
}
