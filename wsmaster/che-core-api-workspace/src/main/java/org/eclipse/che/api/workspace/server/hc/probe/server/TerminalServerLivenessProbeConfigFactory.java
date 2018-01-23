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
package org.eclipse.che.api.workspace.server.hc.probe.server;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.hc.probe.HttpProbeConfig;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;

/**
 * Produces {@link HttpProbeConfig} for terminal agent liveness probes.
 *
 * @author Alexander Garagatyi
 */
public class TerminalServerLivenessProbeConfigFactory implements HttpProbeConfigFactory {

  @Override
  public HttpProbeConfig get(String workspaceId, Server server)
      throws InternalInfrastructureException {
    URI uri;
    try {
      uri = new URI(server.getUrl());
    } catch (URISyntaxException e) {
      throw new InternalInfrastructureException(
          "Terminal agent server liveness probe url is invalid. Error: " + e.getMessage());
    }
    String protocol;
    if ("wss".equals(uri.getScheme())) {
      protocol = "https";
    } else {
      protocol = "http";
    }
    int port;
    if (uri.getPort() == -1) {
      if ("http".equals(protocol)) {
        port = 80;
      } else {
        port = 443;
      }
    } else {
      port = uri.getPort();
    }

    return new HttpProbeConfig(port, uri.getHost(), protocol, "/liveness", null, 1, 3, 120, 10, 10);
  }
}
