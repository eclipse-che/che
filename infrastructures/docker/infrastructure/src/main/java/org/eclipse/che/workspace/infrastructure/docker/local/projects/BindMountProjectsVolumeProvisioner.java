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
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.provision.ProjectsVolumeForWsAgentProvisioner;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ConfigurationProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.volume.VolumeNames;
import org.slf4j.Logger;

/**
 * Converts projects volume from data volume to bind-mount volume needed for mounting file system on
 * host to a Docker container.
 *
 * <p>Data volume is a volume that keeps its content in a Docker internal storage. When container is
 * stopped/deleted Docker keeps data volume. Bind-mount volume is a mount of some file/folder from
 * Docker host to container. We use this provisioner to store workspace projects on host to allow
 * direct access by user.
 *
 * @author Alexander Garagatyi
 */
public class BindMountProjectsVolumeProvisioner implements ConfigurationProvisioner {
  private static final Logger LOG = getLogger(BindMountProjectsVolumeProvisioner.class);

  private final LocalProjectsFolderPathProvider workspaceFolderPathProvider;
  private final WindowsPathEscaper pathEscaper;
  private final String projectsVolumeOptions;

  @Inject
  public BindMountProjectsVolumeProvisioner(
      LocalProjectsFolderPathProvider workspaceFolderPathProvider,
      WindowsPathEscaper pathEscaper,
      @Nullable @Named("che.docker.volumes_projects_options") String projectsVolumeOptions) {

    this.workspaceFolderPathProvider = workspaceFolderPathProvider;
    this.pathEscaper = pathEscaper;
    if (!Strings.isNullOrEmpty(projectsVolumeOptions)) {
      this.projectsVolumeOptions = ":" + projectsVolumeOptions;
    } else {
      this.projectsVolumeOptions = "";
    }
  }

  @Override
  public void provision(DockerEnvironment internalEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    for (Entry<String, DockerContainerConfig> containerEntry :
        internalEnv.getContainers().entrySet()) {

      DockerContainerConfig value = containerEntry.getValue();
      List<String> newVolumes = new ArrayList<>();
      for (String volume : value.getVolumes()) {
        String[] volumeSourceTarget = volume.split(":");
        if (VolumeNames.matches(
            volumeSourceTarget[0],
            ProjectsVolumeForWsAgentProvisioner.PROJECTS_VOLUME_NAME,
            identity.getWorkspaceId())) {
          newVolumes.add(getProjectsVolumeSpec(identity.getWorkspaceId(), volumeSourceTarget[1]));
        } else {
          newVolumes.add(volume);
        }
      }
      value.setVolumes(newVolumes);
    }
  }

  // bind-mount volume for projects in a container
  private String getProjectsVolumeSpec(String workspaceId, String path)
      throws InfrastructureException {
    String projectsHostPath;
    try {
      projectsHostPath = workspaceFolderPathProvider.getPath(workspaceId);
    } catch (IOException e) {
      LOG.error(e.getLocalizedMessage(), e);
      throw new InternalInfrastructureException(
          "Error occurred on resolving path to files of workspace " + workspaceId);
    }
    String volumeSpec = format("%s:%s%s", projectsHostPath, path, projectsVolumeOptions);
    return SystemInfo.isWindows() ? pathEscaper.escapePath(volumeSpec) : volumeSpec;
  }
}
