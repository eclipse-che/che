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
