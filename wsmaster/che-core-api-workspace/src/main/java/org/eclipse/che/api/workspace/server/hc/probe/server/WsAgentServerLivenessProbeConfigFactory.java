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
package org.eclipse.che.api.workspace.server.hc.probe.server;

import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.hc.probe.HttpProbeConfig;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Produces {@link HttpProbeConfig} for ws-agent liveness probes.
 *
 * @author Alexander Garagatyi
 */
public class WsAgentServerLivenessProbeConfigFactory implements HttpProbeConfigFactory {

  @Override
  public HttpProbeConfig get(Server server) throws MalformedURLException {
    URL url = new URL(server.getUrl());

    return new HttpProbeConfig(
        url.getPort(), url.getHost(), url.getProtocol(), url.getPath() + "/liveness", 1, 3, 120, 10, 10);
  }
}
