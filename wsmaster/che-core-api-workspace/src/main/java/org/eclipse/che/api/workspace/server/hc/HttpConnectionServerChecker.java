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
package org.eclipse.che.api.workspace.server.hc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.commons.proxy.ProxyAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server checker that uses http connection response code as a criteria of availability of a server.
 * If response code is not less than 200 and less than 400 server is treated as available.
 *
 * @author Alexander Garagatyi
 */
public class HttpConnectionServerChecker extends ServerChecker {
  private static final Logger LOG = LoggerFactory.getLogger(HttpConnectionServerChecker.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String CONNECTION_HEADER = "Connection";
  private static final String CONNECTION_CLOSE = "close";
  private final URL url;
  private final String token;
  private final String serverRef;

  public HttpConnectionServerChecker(
      URL url,
      String machineName,
      String serverRef,
      long period,
      long timeout,
      int successThreshold,
      TimeUnit timeUnit,
      Timer timer,
      String token) {
    super(machineName, serverRef, period, timeout, successThreshold, timeUnit, timer);
    this.url = url;
    this.serverRef = serverRef;
    this.token = token;
  }

  @Override
  public boolean isAvailable() {
    HttpURLConnection httpURLConnection = null;
    try {
      ProxyAuthenticator.initAuthenticator(url.toString());
      httpURLConnection = createConnection(url);
      // TODO consider how much time we should use as a limit
      httpURLConnection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(3));
      httpURLConnection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(3));
      httpURLConnection.setRequestProperty(CONNECTION_HEADER, CONNECTION_CLOSE);
      if (token != null) {
        httpURLConnection.setRequestProperty(AUTHORIZATION_HEADER, "Bearer " + token);
      }
      return isConnectionSuccessful(httpURLConnection);
    } catch (IOException e) {
      LOG.debug(
          "Failed to establish http connection to check server '{}'. Cause: {}",
          serverRef,
          e.getMessage());
      return false;
    } finally {
      ProxyAuthenticator.resetAuthenticator();
      if (httpURLConnection != null) {
        httpURLConnection.disconnect();
      }
    }
  }

  boolean isConnectionSuccessful(HttpURLConnection conn) {
    try {
      int responseCode = conn.getResponseCode();
      boolean success = isConnectionSuccessful(responseCode);

      if (!success && LOG.isDebugEnabled()) {
        String response;
        try {
          InputStream in = conn.getErrorStream();
          if (in == null) {
            in = conn.getInputStream();
          }

          try (Reader reader = new InputStreamReader(in)) {
            response = CharStreams.toString(reader);
          }
        } catch (Exception e) {
          response = "failed to ready response: " + e.getMessage();
        }
        LOG.debug(
            "Server check for '{}:{}' request failed with code {}. Response: {}",
            serverRef,
            url,
            responseCode,
            response);
      }

      return success;
    } catch (IOException e) {
      LOG.debug(
          "Failed to establish http connection to check server '{}:{}'. Cause: {}",
          serverRef,
          url,
          e.getMessage());
      return false;
    }
  }

  boolean isConnectionSuccessful(int responseCode) {
    return responseCode >= 200 && responseCode < 400;
  }

  @VisibleForTesting
  HttpURLConnection createConnection(URL url) throws IOException {
    return (HttpURLConnection) url.openConnection();
  }
}
