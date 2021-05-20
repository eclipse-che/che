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
package org.eclipse.che.api.workspace.server.devfile;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class URLFileContentProviderTest {

  @Mock private URLFetcher urlFetcher;

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "It is unable to fetch a file /relative/dev.yaml as relative to devfile, since devfile location is unknown. Try specifying absolute URL.")
  public void shouldThrowExceptionWhenNoDevfileLocationKnownAndURLIsRelative() throws Exception {
    URLFileContentProvider provider = new URLFileContentProvider(null, urlFetcher);
    provider.fetchContent("/relative/dev.yaml");
  }

  @Test
  public void shouldFetchByAbsoluteURL() throws Exception {
    String url = "http://myhost.com/relative/dev.yaml";
    URLFileContentProvider provider = new URLFileContentProvider(null, urlFetcher);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    provider.fetchContent(url);
    verify(urlFetcher).fetch(captor.capture());
    assertEquals(captor.getValue(), url);
  }

  @Test
  public void shouldMergeDevfileLocationAndRelativeURL() throws Exception {
    String devfileUrl = "http://myhost.com/relative/devile.yaml";
    String relativeUrl = "relative.yaml";
    URLFileContentProvider provider = new URLFileContentProvider(new URI(devfileUrl), urlFetcher);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    provider.fetchContent(relativeUrl);
    verify(urlFetcher).fetch(captor.capture());
    assertEquals(captor.getValue(), "http://myhost.com/relative/relative.yaml");
  }
}
