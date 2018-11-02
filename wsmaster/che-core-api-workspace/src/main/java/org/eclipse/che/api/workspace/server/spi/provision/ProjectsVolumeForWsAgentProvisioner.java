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
package org.eclipse.che.api.workspace.server.spi.provision;

import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds projects volumes to a machine with 'ws-agent' server.
 *
 * @author Alexander Garagatyi
 */
public class ProjectsVolumeForWsAgentProvisioner implements InternalEnvironmentProvisioner {

  private static final Logger LOG = LoggerFactory
      .getLogger(ProjectsVolumeForWsAgentProvisioner.class);
  public static final String PROJECTS_VOLUME_NAME = "projects";

  private final String projectFolderPath;

  @Inject
  public ProjectsVolumeForWsAgentProvisioner(
      @Named("che.workspace.projects.storage") String projectFolderPath) {
    this.projectFolderPath =
        projectFolderPath.startsWith("/") ? projectFolderPath : "/" + projectFolderPath;
  }

  @Override
  public void provision(RuntimeIdentity id, InternalEnvironment internalEnvironment)
      throws InfrastructureException {
    LOG.debug("Provisioning project volumes for workspace '{}'", id.getWorkspaceId());
    Optional<String> wsAgentServerMachine =
        WsAgentMachineFinderUtil.getWsAgentServerMachine(internalEnvironment);

    if (wsAgentServerMachine.isPresent()) {
      InternalMachineConfig machineConfig =
          internalEnvironment.getMachines().get(wsAgentServerMachine.get());
      machineConfig
          .getVolumes()
          .put(PROJECTS_VOLUME_NAME, new VolumeImpl().withPath(projectFolderPath));
    }
  }
}
