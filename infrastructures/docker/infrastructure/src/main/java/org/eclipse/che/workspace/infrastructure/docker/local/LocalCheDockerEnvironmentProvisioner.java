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
package org.eclipse.che.workspace.infrastructure.docker.local;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.DockerEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.dod.DockerApiHostEnvVariableProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.installer.LocalInstallersBinariesVolumeProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.installer.WsAgentServerConfigProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.projects.BindMountProjectsVolumeProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisionersApplier;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.labels.RuntimeLabelsProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.memory.MemoryAttributeConverter;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.server.ServersConverter;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.volume.VolumesConverter;

/**
 * Infrastructure provisioner that apply needed configuration to docker containers to run it
 * locally.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class LocalCheDockerEnvironmentProvisioner implements DockerEnvironmentProvisioner {

  private final ContainerSystemSettingsProvisionersApplier dockerSettingsProvisioners;
  private final BindMountProjectsVolumeProvisioner hostMountingProjectsVolumeProvisioner;
  private final LocalInstallersBinariesVolumeProvisioner installersBinariesVolumeProvisioner;
  private final RuntimeLabelsProvisioner runtimeLabelsProvisioner;
  private final DockerApiHostEnvVariableProvisioner dockerApiEnvProvisioner;
  private final WsAgentServerConfigProvisioner wsAgentServerConfigProvisioner;
  private final ServersConverter serversConverter;
  private final EnvVarsConverter envVarsConverter;
  private final MemoryAttributeConverter memoryAttributeConverter;
  private final VolumesConverter volumesConverter;

  @Inject
  public LocalCheDockerEnvironmentProvisioner(
      ContainerSystemSettingsProvisionersApplier dockerSettingsProvisioners,
      BindMountProjectsVolumeProvisioner hostMountingProjectsVolumeProvisioner,
      LocalInstallersBinariesVolumeProvisioner installersBinariesVolumeProvisioner,
      RuntimeLabelsProvisioner runtimeLabelsProvisioner,
      DockerApiHostEnvVariableProvisioner dockerApiEnvProvisioner,
      WsAgentServerConfigProvisioner wsAgentServerConfigProvisioner,
      ServersConverter serversConverter,
      EnvVarsConverter envVarsConverter,
      MemoryAttributeConverter memoryAttributeConverter,
      VolumesConverter volumesConverter) {

    this.dockerSettingsProvisioners = dockerSettingsProvisioners;
    this.hostMountingProjectsVolumeProvisioner = hostMountingProjectsVolumeProvisioner;
    this.installersBinariesVolumeProvisioner = installersBinariesVolumeProvisioner;
    this.runtimeLabelsProvisioner = runtimeLabelsProvisioner;
    this.dockerApiEnvProvisioner = dockerApiEnvProvisioner;
    this.wsAgentServerConfigProvisioner = wsAgentServerConfigProvisioner;
    this.serversConverter = serversConverter;
    this.envVarsConverter = envVarsConverter;
    this.memoryAttributeConverter = memoryAttributeConverter;
    this.volumesConverter = volumesConverter;
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    // 1 stage - converting Che model env to docker env
    serversConverter.provision(internalEnv, identity);
    envVarsConverter.provision(internalEnv, identity);
    volumesConverter.provision(internalEnv, identity);
    memoryAttributeConverter.provision(internalEnv, identity);

    // 2 stage - add/modify docker env items
    runtimeLabelsProvisioner.provision(internalEnv, identity);
    installersBinariesVolumeProvisioner.provision(internalEnv, identity);
    hostMountingProjectsVolumeProvisioner.provision(internalEnv, identity);
    dockerApiEnvProvisioner.provision(internalEnv, identity);
    wsAgentServerConfigProvisioner.provision(internalEnv, identity);
    dockerSettingsProvisioners.provision(internalEnv, identity);
    dockerApiEnvProvisioner.provision(internalEnv, identity);
  }
}
