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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Produces {@link HttpProbe} instances
 *
 * @see ProbeFactory
 * @author Alexander Garagatyi
 */
public class HttpProbeFactory extends ProbeFactory {
  private final URL url;
  private final int timeout;
  private final Map<String, String> headers;
  private final HttpProbeConfig probeConfig;

  public HttpProbeFactory(
      String workspaceId, String machineName, String serverName, HttpProbeConfig probeConfig)
      throws MalformedURLException {
    super(workspaceId, machineName, serverName, probeConfig);
    url =
        new URL(
            probeConfig.getScheme(),
            probeConfig.getHost(),
            probeConfig.getPort(),
            probeConfig.getPath());
    timeout = (int) TimeUnit.SECONDS.toMillis(probeConfig.getTimeoutSeconds());
    headers = probeConfig.getHeaders();
    this.probeConfig = probeConfig;
  }

  @Override
  public HttpProbeConfig getProbeConfig() {
    return probeConfig;
  }

  @Override
  public HttpProbe get() {
    return new HttpProbe(url, timeout, headers);
  }
}
