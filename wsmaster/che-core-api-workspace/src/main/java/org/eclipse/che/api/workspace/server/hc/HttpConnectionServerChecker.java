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
package org.eclipse.che.api.workspace.server.hc;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Server checker that uses http connection response code as a criteria of availability of a server.
 * If response code is not less than 200 and less than 400 server is treated as available.
 *
 * @author Alexander Garagatyi
 */
public class HttpConnectionServerChecker extends ServerChecker {
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String CONNECTION_HEADER = "Connection";
  private static final String CONNECTION_CLOSE = "close";
  private final URL url;
  private final String token;

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
    this.token = token;
  }

  @Override
  public boolean isAvailable() {
    HttpURLConnection httpURLConnection = null;
    try {
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
      return false;
    } finally {
      if (httpURLConnection != null) {
        httpURLConnection.disconnect();
      }
    }
  }

  boolean isConnectionSuccessful(HttpURLConnection conn) {
    try {
      int responseCode = conn.getResponseCode();
      return responseCode >= 200 && responseCode < 400;
    } catch (IOException e) {
      return false;
    }
  }

  @VisibleForTesting
  HttpURLConnection createConnection(URL url) throws IOException {
    return (HttpURLConnection) url.openConnection();
  }
}
