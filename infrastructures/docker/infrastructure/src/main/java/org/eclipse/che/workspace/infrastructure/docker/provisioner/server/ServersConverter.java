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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.server;

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.InfrastructureProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.Labels;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Converts {@link ServerConfig} to Docker related objects to add a server into Docker runtime.
 *
 * <p>Adds ports mapping, exposes, labels to Docker container config to be able to evaluate {@link
 * Server}.
 *
 * @author Alexander Garagatyi
 */
public class ServersConverter implements InfrastructureProvisioner {

  @Override
  public void provision(
      InternalEnvironment environment, DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    environment
        .getMachines()
        .forEach(
            (machineName, machineConfig) -> {
              DockerContainerConfig container = internalEnv.getContainers().get(machineName);

              container
                  .getLabels()
                  .putAll(Labels.newSerializer().servers(machineConfig.getServers()).labels());

              machineConfig
                  .getServers()
                  .forEach(
                      (key, value) -> {
                        container.getPorts().add(value.getPort());
                        container.getExpose().add(value.getPort());
                      });
            });
  }
}
