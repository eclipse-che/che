/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.hc.probe;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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

  private HttpURLConnection httpURLConnection;

  /**
   * Creates probe
   *
   * @param url HTTP endpoint to probe
   * @param timeout connection and read timeouts
   */
  public HttpProbe(URL url, int timeout) {
    this.url = url;
    this.timeout = timeout;
  }

  @Override
  public boolean probe() {
    try {
      httpURLConnection = (HttpURLConnection) url.openConnection();
      httpURLConnection.setConnectTimeout(timeout);
      httpURLConnection.setReadTimeout(timeout);
      httpURLConnection.setRequestProperty(CONNECTION_HEADER, CONNECTION_CLOSE);
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
