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
package org.eclipse.che.api.workspace.server.hc.probe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Probes a HTTP(s) URL for a response with code >=200 and <400
 *
 * @author Alexander Garagatyi
 */
public class HttpProbe extends Probe {

  private static final String CONNECTION_HEADER = "Connection";
  private static final String CONNECTION_CLOSE = "close";

  private final URL url;
  private final int timeout;
  private final Map<String, String> headers;

  private HttpURLConnection httpURLConnection;

  /**
   * Creates probe
   *
   * @param url HTTP endpoint to probe
   * @param timeout connection and read timeouts
   */
  public HttpProbe(URL url, int timeout, Map<String, String> headers) {
    this.url = url;
    this.timeout = timeout;
    this.headers = new HashMap<>();
    if (headers != null) {
      this.headers.putAll(headers);
    }
    this.headers.put(CONNECTION_HEADER, CONNECTION_CLOSE);
  }

  @Override
  public boolean doProbe() {
    try {
      httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setConnectTimeout(timeout);
      httpURLConnection.setReadTimeout(timeout);
      headers.forEach((name, value) -> httpURLConnection.setRequestProperty(name, value));
      return isConnectionSuccessful(httpURLConnection);
    } catch (IOException e) {
      return false;
    } finally {
      if (httpURLConnection != null) {
        httpURLConnection.disconnect();
      }
    }
  }

  /**
   * More effectively cancels the probe than cancellation inherited from {@link Probe}.
   *
   * @see Probe#cancel()
   */
  @Override
  public void cancel() {
    httpURLConnection.disconnect();
  }

  private boolean isConnectionSuccessful(HttpURLConnection conn) {
    try {
      int responseCode = conn.getResponseCode();
      return responseCode >= 200 && responseCode < 400;
    } catch (IOException e) {
      return false;
    }
  }
}
