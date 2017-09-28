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
package org.eclipse.che.api.project.server;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.server.api.ProjectConfigRegistry;

/**
 * Synchronizer for Project Configurations stored in Workspace Configuration with Installer's state
 *
 * @author gazarenkov
 */
public abstract class WorkspaceProjectsSyncer {

  private final ProjectConfigRegistry projectConfigs;
  private final WorkspaceSyncCommunication workspaceSyncCommunication;

  protected WorkspaceProjectsSyncer(ProjectConfigRegistry projectConfigs,
      WorkspaceSyncCommunication workspaceSyncCommunication) {
    this.projectConfigs = projectConfigs;
    this.workspaceSyncCommunication = workspaceSyncCommunication;
  }

  /**
   * Synchronizes Project Config state on Agent and Master
   *
   * @throws ServerException
   */
  public final void sync() throws ServerException {

    List<? extends ProjectConfig> remote = getProjects();

    // check on removed
    List<ProjectConfig> removed = new ArrayList<>();
    for (ProjectConfig r : remote) {
      if (!projectConfigs.get(r.getPath()).isPresent()) {
        removed.add(r);
      }
    }

    for (ProjectConfig r : removed) {
      removeProject(r);
    }

    // update or add
    for (RegisteredProject project : projectConfigs.getAll()) {

      if (!project.isSynced() && !project.isDetected()) {

        final ProjectConfig config =
            new NewProjectConfigImpl(
                project.getPath(),
                project.getType(),
                project.getMixins(),
                project.getName(),
                project.getDescription(),
                project.getPersistableAttributes(),
                null,
                project.getSource());

        boolean found = false;
        for (ProjectConfig r : remote) {
          if (r.getPath().equals(project.getPath())) {
            updateProject(config);
            found = true;
          }
        }

        if (!found) addProject(config);

        project.setSync();
      }
    }
    workspaceSyncCommunication.synchronizeWorkspace();
  }

  /**
   * @return projects from Workspace Config
   * @throws ServerException
   */
  public abstract List<? extends ProjectConfig> getProjects() throws ServerException;

  /** @return workspace ID */
  public abstract String getWorkspaceId();

  /**
   * Adds project to Workspace Config
   *
   * @param project the project config
   * @throws ServerException
   */
  protected abstract void addProject(ProjectConfig project) throws ServerException;

  /**
   * Updates particular project in Workspace Config
   *
   * @param project the project config
   * @throws ServerException
   */
  protected abstract void updateProject(ProjectConfig project) throws ServerException;

  /**
   * Removes particular project in Workspace Config
   *
   * @param project the project config
   * @throws ServerException
   */
  protected abstract void removeProject(ProjectConfig project) throws ServerException;
}
