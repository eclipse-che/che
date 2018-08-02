/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.hc.probe;

import static com.google.common.base.Strings.isNullOrEmpty;

/** @author Alexander Garagatyi */
public class TcpProbeConfig extends ProbeConfig {

  private final int port;
  private final String host;

  /**
   * Creates probe configuration.
   *
   * @param port port of the TCP server to probe
   * @param host hostname of the TCP server to probe
   * @see ProbeConfig#ProbeConfig(int, int, int, int, int)
   */
  public TcpProbeConfig(
      int successThreshold,
      int failureThreshold,
      int timeoutSeconds,
      int periodSeconds,
      int initialDelaySeconds,
      int port,
      String host) {
    super(successThreshold, failureThreshold, timeoutSeconds, periodSeconds, initialDelaySeconds);
    if (port < 1) {
      throw new IllegalArgumentException(
          "Port '" + port + "' is illegal. Port should not be less than 1");
    }
    this.port = port;
    if (isNullOrEmpty(host)) {
      throw new IllegalArgumentException(
          "Host '" + host + "' is illegal. Host should not be empty");
    }
    this.host = host;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }
}
