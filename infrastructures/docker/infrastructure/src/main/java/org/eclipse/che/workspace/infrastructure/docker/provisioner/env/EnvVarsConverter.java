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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.env;

import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.DockerEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Converts environment variables in {@link MachineConfig} to Docker environment variables.
 *
 * @author Alexander Garagatyi
 */
public class EnvVarsConverter implements DockerEnvironmentProvisioner {

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    internalEnv
        .getMachines()
        .forEach(
            (machineName, machineConfig) ->
                internalEnv
                    .getContainers()
                    .get(machineName)
                    .getEnvironment()
                    .putAll(machineConfig.getEnv()));
  }
}
