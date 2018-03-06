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

import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.hc.probe.HttpProbeConfig;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;

/**
 * Produces {@link HttpProbeConfig} for a server liveness probes.
 *
 * @author Alexander Garagatyi
 * @apiNote this is a workaround needed because Che agents doesn't have a way to specify liveness
 *     probes configuration. We should remove this when it is possible to configure ws-agent, exec
 *     and terminal agents probes configuration
 */
public interface HttpProbeConfigFactory {

  /**
   * Returns liveness probe config for a specified server
   *
   * @throws InternalInfrastructureException when server probe creation failed
   */
  HttpProbeConfig get(String workspaceId, Server server) throws InternalInfrastructureException;

  HttpProbeConfig get(String userId, String workspaceId, Server server)
      throws InternalInfrastructureException;
}
