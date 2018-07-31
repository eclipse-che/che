/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.provisioner.volume;

import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.DockerEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

/**
 * Converts volumes from {@link MachineConfig} to Docker volumes unique for each workspace.
 *
 * @author Alexander Garagatyi
 */
public class VolumesConverter implements DockerEnvironmentProvisioner {

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    internalEnv
        .getMachines()
        .forEach(
            (machineName, machineConfig) -> {
              DockerContainerConfig containerConfig = internalEnv.getContainers().get(machineName);
              machineConfig
                  .getVolumes()
                  .forEach(
                      (volumeName, volume) -> {
                        String uniqueVolumeName =
                            VolumeNames.generate(identity.getWorkspaceId(), volumeName);
                        containerConfig.getVolumes().add(uniqueVolumeName + ':' + volume.getPath());
                      });
            });
  }
}
