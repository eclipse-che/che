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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allow to grab content from URL
 *
 * @author Florent Benoit
 */
@Singleton
public class URLFetcher {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(URLFetcher.class);

  /** Maximum size of allowed data. (80KB) */
  protected static final long MAXIMUM_READ_BYTES = 80 * 1024;

  /** timeout when reading */
  @VisibleForTesting static final int CONNECTION_READ_TIMEOUT = 10 * 1000; // 10s

  /** The Compiled REGEX PATTERN that can be used for http|https git urls */
  final Pattern GIT_HTTP_URL_PATTERN = Pattern.compile("(?<sanitized>^http[s]?://.*)\\.git$");

  /**
   * Fetches the url provided and return its content. To prevent DOS attack, limit the amount of the
   * collected data
   *
   * @param url the URL to fetch
   * @return the content of the requested URL or {@code null} if error happened
   */
  public String fetchSafely(@NotNull final String url) {
    requireNonNull(url, "url parameter can't be null");
    try {
      return fetch(url);
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Fetches the url provided and return its content. Uses default read connection timeout {@link
   * URLFetcher#CONNECTION_READ_TIMEOUT}
   *
   * @param url the URL to fetch
   * @return content of the requested URL
   * @throws IOException if fetch error occurs
   */
  public String fetch(@NotNull final String url) throws IOException {
    return fetch(url, CONNECTION_READ_TIMEOUT);
  }
  /**
   * Fetches the url provided and return its content. Uses default read connection timeout {@link
   * URLFetcher#CONNECTION_READ_TIMEOUT}
   *
   * @param url the URL to fetch
   * @return content of the requested URL
   * @throws IOException if fetch error occurs
   */
  public String fetch(@NotNull final String url, String authorization) throws IOException {
    return fetch(url, CONNECTION_READ_TIMEOUT, authorization);
  }

  /**
   * Fetches the url provided and return its content.
   *
   * @param url the URL to fetch
   * @param timeout read and connection timeout (see {@link URLConnection#setConnectTimeout(int)}
   *     and {@link URLConnection#setReadTimeout(int)}
   * @return content of the requested URL
   * @throws IOException if fetch error occurs
   */
  String fetch(@NotNull final String url, int timeout) throws IOException {
    return fetch(url, timeout, null);
  }

  /**
   * Fetches the url provided and return its content.
   *
   * @param url the URL to fetch
   * @param timeout read and connection timeout (see {@link URLConnection#setConnectTimeout(int)}
   *     and {@link URLConnection#setReadTimeout(int)}
   * @return content of the requested URL
   * @throws IOException if fetch error occurs
   */
  String fetch(@NotNull final String url, int timeout, String authorization) throws IOException {
    requireNonNull(url, "url parameter can't be null");
    URLConnection connection = new URL(sanitized(url)).openConnection();
    connection.setConnectTimeout(timeout);
    connection.setReadTimeout(timeout);
    if (!isNullOrEmpty(authorization)) {
      connection.setRequestProperty(HttpHeaders.AUTHORIZATION, authorization);
    }
    return fetch(connection);
  }

  /**
   * Fetch the urlConnection stream by using the urlconnection and return its content To prevent DOS
   * attack, limit the amount of the collected data
   *
   * @param urlConnection the URL connection to fetch
   * @return the content of the file
   * @throws IOException if fetch error occurs
   */
  @VisibleForTesting
  String fetch(@NotNull URLConnection urlConnection) throws IOException {
    requireNonNull(urlConnection, "urlConnection parameter can't be null");
    final String value;
    try (InputStream inputStream = urlConnection.getInputStream();
        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(ByteStreams.limit(inputStream, getLimit()), UTF_8))) {
      value = reader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      // we shouldn't fetch if check is done before
      LOG.debug("Invalid URL", e);
      throw e;
    }
    return value;
  }

  /**
   * Maximum size that can be read.
   *
   * @return maximum size.
   */
  protected long getLimit() {
    return MAXIMUM_READ_BYTES;
  }

  /**
   * Simple method to sanitize the Git urls like &quot;https://github.com/demo.git&quot; or
   * &quot;http://myowngit.example.com/demo.git&quot;
   *
   * @param url - the String format of the url
   * @return if the url ends with .git will return the url without .git otherwise return the url as
   *     it is
   */
  @VisibleForTesting
  String sanitized(String url) {
    if (url != null) {
      final Matcher matcher = GIT_HTTP_URL_PATTERN.matcher(url);
      if (matcher.find()) {
        return matcher.group("sanitized");
      }
    }
    return url;
  }
}
