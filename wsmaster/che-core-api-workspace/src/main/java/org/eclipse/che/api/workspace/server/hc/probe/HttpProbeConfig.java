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
package org.eclipse.che.api.workspace.server.hc.probe;

import static java.util.Collections.emptyMap;

import java.util.Map;

/**
 * Configuration of a HTTP URL probe.
 *
 * @author Alexander Garagatyi
 */
public class HttpProbeConfig extends TcpProbeConfig {

  private final String scheme;
  private final String path;
  private final Map<String, String> headers;

  /**
   * Creates probe configuration.
   *
   * @param scheme protocol of the HTTP server (http or https)
   * @param path path for the HTTP probe
   * @param headers optional headers to add into the HTTP probe request
   * @see TcpProbeConfig#TcpProbeConfig(int, int, int, int, int, int, String)
   */
  public HttpProbeConfig(
      int port,
      String host,
      String scheme,
      String path,
      Map<String, String> headers,
      int successThreshold,
      int failureThreshold,
      int timeoutSeconds,
      int periodSeconds,
      int initialDelaySeconds) {
    super(
        successThreshold,
        failureThreshold,
        timeoutSeconds,
        periodSeconds,
        initialDelaySeconds,
        port,
        host);
    if (!"http".equals(scheme) && !"https".equals(scheme)) {
      throw new IllegalArgumentException("HTTP probe scheme must be 'http' or 'https'");
    }
    this.scheme = scheme;
    this.path = path;
    this.headers = headers != null ? headers : emptyMap();
  }

  public String getScheme() {
    return scheme;
  }

  public String getPath() {
    return path;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }
}
