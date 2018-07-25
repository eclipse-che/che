/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.urlfactory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

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
import org.apache.commons.compress.utils.BoundedInputStream;
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

  /** Maximum size of allowed data. (30KB) */
  protected static final long MAXIMUM_READ_BYTES = 30 * 1000;

  /** The Compiled REGEX PATTERN that can be used for http|https git urls */
  final Pattern GIT_HTTP_URL_PATTERN = Pattern.compile("(?<sanitized>^http[s]?://.*)\\.git$");

  /**
   * Fetch the url provided and return its content To prevent DOS attack, limit the amount of the
   * collected data
   *
   * @param url the URL to fetch
   * @return the content of the file
   */
  public String fetch(@NotNull final String url) {
    requireNonNull(url, "url parameter can't be null");
    try {
      return fetch(new URL(sanitized(url)).openConnection());
    } catch (IOException e) {
      // we shouldn't fetch if check is done before
      LOG.debug("Invalid URL", e);
      return null;
    }
  }

  /**
   * Fetch the urlConnection stream by using the urlconnection and return its content To prevent DOS
   * attack, limit the amount of the collected data
   *
   * @param urlConnection the URL connection to fetch
   * @return the content of the file
   */
  public String fetch(@NotNull URLConnection urlConnection) {
    requireNonNull(urlConnection, "urlConnection parameter can't be null");
    final String value;
    try (InputStream inputStream = urlConnection.getInputStream();
        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(new BoundedInputStream(inputStream, getLimit()), UTF_8))) {
      value = reader.lines().collect(Collectors.joining("\n"));
    } catch (IOException e) {
      // we shouldn't fetch if check is done before
      LOG.debug("Invalid URL", e);
      return null;
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
