/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.che.api.workspace.server.devfile.URLFetcher.CONNECTION_READ_TIMEOUT;
import static org.eclipse.che.api.workspace.server.devfile.URLFetcher.MAXIMUM_READ_BYTES;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.google.common.base.Strings;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Testing {@link org.eclipse.che.api.workspace.server.devfile.URLFetcher}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class URLFetcherTest {

  /** Instance to test. */
  @InjectMocks private URLFetcher urlFetcher;

  /** Check that when url is null, NPE is thrown */
  @Test(expectedExceptions = NullPointerException.class)
  public void checkNullURL() {
    urlFetcher.fetchSafely(null);
  }

  /** Check that when url exists the content is retrieved */
  @Test
  public void checkGetContent() {

    // test to download this class object
    URL urlJson = getClass().getClassLoader().getResource("devfile/url_fetcher_test_resource.json");
    Assert.assertNotNull(urlJson);

    String content = urlFetcher.fetchSafely(urlJson.toString());
    assertEquals(content, "Hello");
  }

  /** Check when url is invalid */
  @Test
  public void checkUrlFileIsInvalid() {
    String result = urlFetcher.fetchSafely("hello world");
    assertNull(result);
  }

  /** Check when url is invalid */
  @Test(
      expectedExceptions = IOException.class,
      expectedExceptionsMessageRegExp = "no protocol: hello_world")
  public void checkUnsafeGetUrlFileIsInvalid() throws Exception {
    String result = urlFetcher.fetch("hello_world");
    assertNull(result);
  }

  /** Check Sanitizing of Git URL works */
  @Test
  public void checkDotGitRemovedFromURL() {
    String result = urlFetcher.sanitized("https://github.com/acme/demo.git");
    assertEquals("https://github.com/acme/demo", result);

    result = urlFetcher.sanitized("http://github.com/acme/demo.git");
    assertEquals("http://github.com/acme/demo", result);
  }

  /** Check that when url doesn't exist */
  @Test
  public void checkMissingContent() {

    // test to download this class object
    URL urlJson = getClass().getClassLoader().getResource("devfile/url_fetcher_test_resource.json");
    Assert.assertNotNull(urlJson);

    // add extra path to make url not found
    String content = urlFetcher.fetchSafely(urlJson.toString() + "-invalid");
    assertNull(content);
  }

  /** Check that when url doesn't exist */
  @Test(
      expectedExceptions = IOException.class,
      expectedExceptionsMessageRegExp =
          ".*url_fetcher_test_resource.json-invalid \\(No such file or directory\\)")
  public void checkMissingContentUnsafeGet() throws Exception {

    // test to download this class object
    URL urlJson = getClass().getClassLoader().getResource("devfile/url_fetcher_test_resource.json");
    Assert.assertNotNull(urlJson);

    // add extra path to make url not found
    String content = urlFetcher.fetch(urlJson.toString() + "-invalid");
    assertNull(content);
  }

  /** Check when we reach custom limit */
  @Test
  public void checkPartialContent() {
    URL urlJson = getClass().getClassLoader().getResource("devfile/url_fetcher_test_resource.json");
    Assert.assertNotNull(urlJson);

    String content = new OneByteURLFetcher().fetchSafely(urlJson.toString());
    assertEquals(content, "Hello".substring(0, 1));
  }

  /** Check when we reach custom limit */
  @Test
  public void checkDefaultPartialContent() throws IOException {
    URLConnection urlConnection = Mockito.mock(URLConnection.class);
    String originalContent = Strings.padEnd("", (int) MAXIMUM_READ_BYTES, 'a');
    String extraContent = originalContent + "----";
    when(urlConnection.getInputStream())
        .thenReturn(new ByteArrayInputStream(extraContent.getBytes(UTF_8)));
    String readcontent = urlFetcher.fetch(urlConnection);
    // check extra content has been removed as we keep only first values
    assertEquals(readcontent, originalContent);
  }

  @Test
  public void testDefaultFetchTimeoutIsSet() throws IOException {
    URLFetcher fetcher =
        new TimeoutCheckURLFetcher(
            timeout -> assertEquals(timeout.intValue(), CONNECTION_READ_TIMEOUT));

    fetcher.fetch("http://eclipse.org/che");
  }

  @Test
  public void testFetchTimeoutIsSet() throws IOException {
    URLFetcher fetcher =
        new TimeoutCheckURLFetcher(timeout -> assertEquals(timeout.intValue(), 123));

    fetcher.fetch("http://eclipse.org/che", 123);
  }

  @Test(expectedExceptions = IOException.class)
  public void testExceptionIsThrownOnTimeout() throws IOException {
    URLFetcher fetcher = new URLFetcher();
    URLConnection connection =
        new URLConnection(new URL("http://eclipse.org/che")) {
          @Override
          public void connect() throws IOException {
            // noop
          }

          @Override
          public InputStream getInputStream() throws IOException {
            throw new SocketTimeoutException("yes");
          }
        };

    fetcher.fetch(connection);
  }

  /** Limit to only one Byte. */
  static class OneByteURLFetcher extends URLFetcher {
    /** Override the limit */
    @Override
    protected long getLimit() {
      return 1;
    }
  }

  private static class TimeoutCheckURLFetcher extends URLFetcher {
    private final Consumer<Integer> assertion;

    public TimeoutCheckURLFetcher(Consumer<Integer> assertion) {
      this.assertion = assertion;
    }

    @Override
    String fetch(URLConnection urlConnection) {
      assertion.accept(urlConnection.getReadTimeout());
      assertion.accept(urlConnection.getConnectTimeout());
      return "NOOP";
    }
  }
}
