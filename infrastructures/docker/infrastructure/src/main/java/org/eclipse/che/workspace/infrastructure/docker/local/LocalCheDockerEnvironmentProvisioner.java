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
package org.eclipse.che.workspace.infrastructure.docker.local;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.docker.DockerEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.dod.DockerApiHostEnvVariableProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.installer.LocalInstallersBinariesVolumeProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.installer.WsAgentServerConfigProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.projects.ProjectsVolumeProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisionersApplier;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.labels.RuntimeLabelsProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.memory.MemoryAttributeConverter;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.server.ServersConverter;

/**
 * Infrastructure provisioner that apply needed configuration to docker containers to run it
 * locally.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class LocalCheDockerEnvironmentProvisioner implements DockerEnvironmentProvisioner {

  private final ContainerSystemSettingsProvisionersApplier dockerSettingsProvisioners;
  private final ProjectsVolumeProvisioner projectsVolumeProvisioner;
  private final LocalInstallersBinariesVolumeProvisioner installersBinariesVolumeProvisioner;
  private final RuntimeLabelsProvisioner runtimeLabelsProvisioner;
  private final DockerApiHostEnvVariableProvisioner dockerApiEnvProvisioner;
  private final WsAgentServerConfigProvisioner wsAgentServerConfigProvisioner;
  private final ServersConverter serversConverter;
  private final EnvVarsConverter envVarsConverter;
  private final MemoryAttributeConverter memoryAttributeConverter;

  @Inject
  public LocalCheDockerEnvironmentProvisioner(
      ContainerSystemSettingsProvisionersApplier dockerSettingsProvisioners,
      ProjectsVolumeProvisioner projectsVolumeProvisioner,
      LocalInstallersBinariesVolumeProvisioner installersBinariesVolumeProvisioner,
      RuntimeLabelsProvisioner runtimeLabelsProvisioner,
      DockerApiHostEnvVariableProvisioner dockerApiEnvProvisioner,
      WsAgentServerConfigProvisioner wsAgentServerConfigProvisioner,
      ServersConverter serversConverter,
      EnvVarsConverter envVarsConverter,
      MemoryAttributeConverter memoryAttributeConverter) {

    this.dockerSettingsProvisioners = dockerSettingsProvisioners;
    this.projectsVolumeProvisioner = projectsVolumeProvisioner;
    this.installersBinariesVolumeProvisioner = installersBinariesVolumeProvisioner;
    this.runtimeLabelsProvisioner = runtimeLabelsProvisioner;
    this.dockerApiEnvProvisioner = dockerApiEnvProvisioner;
    this.wsAgentServerConfigProvisioner = wsAgentServerConfigProvisioner;
    this.serversConverter = serversConverter;
    this.envVarsConverter = envVarsConverter;
    this.memoryAttributeConverter = memoryAttributeConverter;
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    // 1 stage - add Che business logic items to Che model env
    // 2 stage - converting Che model env to docker env
    serversConverter.provision(internalEnv, identity);
    envVarsConverter.provision(internalEnv, identity);
    memoryAttributeConverter.provision(internalEnv, identity);
    // 3 stage - add docker env items
    runtimeLabelsProvisioner.provision(internalEnv, identity);
    installersBinariesVolumeProvisioner.provision(internalEnv, identity);
    projectsVolumeProvisioner.provision(internalEnv, identity);
    dockerApiEnvProvisioner.provision(internalEnv, identity);
    wsAgentServerConfigProvisioner.provision(internalEnv, identity);
    dockerSettingsProvisioners.provision(internalEnv, identity);
    dockerApiEnvProvisioner.provision(internalEnv, identity);
  }
}
