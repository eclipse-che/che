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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.limits.ram;

import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisioner;

/**
 * Sets RAM size configured in the system as a property in case it is missing in container
 * configuration.
 *
 * @author Alexander Garagatyi
 */
public class DefaultRAMProvisioner implements ContainerSystemSettingsProvisioner {
  private final long defaultMachineMemorySizeBytes;

  @Inject
  public DefaultRAMProvisioner(
      @Named("che.workspace.default_memory_mb") long defaultMachineMemorySizeMB) {
    this.defaultMachineMemorySizeBytes = defaultMachineMemorySizeMB * 1_024 * 1_024;
  }

  @Override
  public void provision(DockerEnvironment internalEnv) throws InfrastructureException {
    for (DockerContainerConfig containerConfig : internalEnv.getContainers().values()) {
      // set default mem limit for container if it is not set
      if (containerConfig.getMemLimit() == null || containerConfig.getMemLimit() == 0) {
        containerConfig.setMemLimit(defaultMachineMemorySizeBytes);
      }
    }
  }
}
