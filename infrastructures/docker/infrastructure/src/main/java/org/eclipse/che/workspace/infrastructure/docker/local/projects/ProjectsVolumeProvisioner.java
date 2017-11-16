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
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;
import org.slf4j.Logger;

/**
 * Provisions environment configuration with a volume needed for mounting file system on host to a
 * docker container.
 *
 * @author Alexander Garagatyi
 */
public class ProjectsVolumeProvisioner implements ConfigurationProvisioner {
  private static final Logger LOG = getLogger(ProjectsVolumeProvisioner.class);

  private final LocalProjectsFolderPathProvider workspaceFolderPathProvider;
  private final WindowsPathEscaper pathEscaper;
  private final String projectFolderPath;
  private final String projectsVolumeOptions;

  @Inject
  public ProjectsVolumeProvisioner(
      LocalProjectsFolderPathProvider workspaceFolderPathProvider,
      WindowsPathEscaper pathEscaper,
      @Named("che.workspace.projects.storage") String projectFolderPath,
      @Nullable @Named("che.docker.volumes_projects_options") String projectsVolumeOptions) {

    this.workspaceFolderPathProvider = workspaceFolderPathProvider;
    this.pathEscaper = pathEscaper;
    this.projectFolderPath = projectFolderPath;
    if (!Strings.isNullOrEmpty(projectsVolumeOptions)) {
      this.projectsVolumeOptions = ":" + projectsVolumeOptions;
    } else {
      this.projectsVolumeOptions = "";
    }
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    Optional<String> devMachineOpt = WsAgentMachineFinderUtil.getWsAgentServerMachine(internalEnv);
    if (!devMachineOpt.isPresent()) {
      // should not happen
      // no wsagent - no projects volume is needed
      return;
    }
    String devMachineName = devMachineOpt.get();

    DockerContainerConfig devMachineConfig = internalEnv.getContainers().get(devMachineName);
    if (devMachineConfig == null) {
      throw new InternalInfrastructureException(
          format("Docker configuration for machine '%s' not found", devMachineName));
    }
    devMachineConfig.getVolumes().add(getProjectsVolumeSpec(identity.getWorkspaceId()));
  }

  // bind-mount volume for projects in a wsagent container
  private String getProjectsVolumeSpec(String workspaceId) throws InfrastructureException {
    String projectsHostPath;
    try {
      projectsHostPath = workspaceFolderPathProvider.getPath(workspaceId);
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
      throw new InternalInfrastructureException(
          "Error occurred on resolving path to files of workspace " + workspaceId);
    }
    String volumeSpec =
        format("%s:%s%s", projectsHostPath, projectFolderPath, projectsVolumeOptions);
    return SystemInfo.isWindows() ? pathEscaper.escapePath(volumeSpec) : volumeSpec;
  }
}
