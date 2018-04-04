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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.memory;

import static java.lang.String.format;

import java.util.Map.Entry;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.docker.DockerEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/** @author Alexander Garagatyi */
public class MemoryAttributeConverter implements DockerEnvironmentProvisioner {

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Entry<String, ? extends InternalMachineConfig> machineEntry :
        internalEnv.getMachines().entrySet()) {
      String machineName = machineEntry.getKey();
      InternalMachineConfig machineConfig = machineEntry.getValue();

      String memory = machineConfig.getAttributes().get(MachineConfig.MEMORY_LIMIT_ATTRIBUTE);
      if (memory != null) {
        try {
          internalEnv.getContainers().get(machineName).setMemLimit(Long.parseLong(memory));
        } catch (NumberFormatException e) {
          throw new InfrastructureException(
              format(
                  "Value of attribute 'memoryLimitBytes' of machine '%s' is illegal", machineName));
        }
      }
    }
  }
}
