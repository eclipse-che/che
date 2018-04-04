/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.urlfactory;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages checking if URL are there or not
 *
 * @author Florent Benoit
 */
@Singleton
public class URLChecker {

  /** Logger. */
  private static final Logger LOG = LoggerFactory.getLogger(URLChecker.class);

  /** Connection timeout of 10seconds. */
  private static final int CONNECTION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(10);

  /** Error message to log. */
  private static final String UNABLE_TO_CHECK_MESSAGE =
      "Unable to check if remote location {0} is available or not. {1}";

  /**
   * Check if given URL location exists remotely
   *
   * @param url the URL to test
   * @return true if remote URL is existing directly (no redirect)
   */
  public boolean exists(@NotNull final String url) {
    requireNonNull(url, "URL parameter cannot be null");
    try {
      return exists(new URL(url));
    } catch (MalformedURLException e) {
      LOG.debug(UNABLE_TO_CHECK_MESSAGE, url, e);
      return false;
    }
  }

  /**
   * Check if given URL location exists remotely
   *
   * @param url the URL to test
   * @return true if remote URL is existing directly (no redirect)
   */
  public boolean exists(@NotNull final URL url) {
    requireNonNull(url, "URL parameter cannot be null");

    try {
      final URLConnection urlConnection = url.openConnection();
      urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
      if (urlConnection instanceof HttpURLConnection) {
        return exists((HttpURLConnection) urlConnection);
      } else {
        urlConnection.connect();
        return true;
      }
    } catch (IOException ioe) {
      LOG.debug(UNABLE_TO_CHECK_MESSAGE, url, ioe);
      return false;
    }
  }

  /**
   * Check if given URL location exists remotely
   *
   * @param httpURLConnection the http url connection to test
   * @return true if remote URL is existing directly (no redirect)
   */
  protected boolean exists(final HttpURLConnection httpURLConnection) {
    try {
      return httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK;
    } catch (IOException ioe) {
      LOG.debug(UNABLE_TO_CHECK_MESSAGE, httpURLConnection, ioe);
      return false;
    } finally {
      httpURLConnection.disconnect();
    }
  }
}
