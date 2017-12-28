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

/** @author Alexander Garagatyi */
public class TcpProbeConfig extends ProbeConfig {

  private final int port;
  private final String host;

  public TcpProbeConfig(
      int successThreshold,
      int failureThreshold,
      int timeoutSeconds,
      int periodSeconds,
      int initialDelaySeconds,
      int port,
      String host) {
    super(successThreshold, failureThreshold, timeoutSeconds, periodSeconds, initialDelaySeconds);
    // TODO check port, host
    this.port = port;
    this.host = host;
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }
}
