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
package org.eclipse.che.api.project.server.impl;

import static org.eclipse.che.api.fs.api.PathResolver.ROOT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.fs.api.FsManager;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.WorkspaceProjectsSyncer;
import org.eclipse.che.api.project.server.api.ProjectConfigRegistry;
import org.eclipse.che.api.project.server.api.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;

@Singleton
public class ProjectInitializer
    implements org.eclipse.che.api.project.server.api.ProjectInitializer {

  private final FsManager fileSystemManager;
  private final WorkspaceProjectsSyncer syncer;
  private final ProjectConfigRegistry projectConfigs;
  private final ProjectHandlerRegistry projectHandlers;

  @Inject
  public ProjectInitializer(
      FsManager fileSystemManager,
      WorkspaceProjectsSyncer syncer,
      ProjectConfigRegistry projectConfigs,
      ProjectHandlerRegistry projectHandlers) {
    this.fileSystemManager = fileSystemManager;
    this.syncer = syncer;
    this.projectConfigs = projectConfigs;
    this.projectHandlers = projectHandlers;
  }

  @Override
  public void initialize()
      throws ConflictException, NotFoundException, ServerException, ForbiddenException,
          IOException {
    initializeRegisteredProjects();
    initializeNotRegisteredProjects();
    firePostInitializationHandlers();
  }

  private void initializeRegisteredProjects()
      throws ConflictException, NotFoundException, ServerException, ForbiddenException,
          IOException {

    List<? extends ProjectConfig> projectConfigs = syncer.getProjects();

    for (ProjectConfig projectConfig : projectConfigs) {
      this.projectConfigs.put(projectConfig, false, false);
    }
  }

  private void initializeNotRegisteredProjects()
      throws ConflictException, NotFoundException, ServerException, ForbiddenException,
          IOException {
    Set<String> wsPaths = fileSystemManager.getDirectoryWsPaths(ROOT);
    for (String wsPath : wsPaths) {
      if (!projectConfigs.isRegistered(wsPath)) {
        projectConfigs.put(wsPath, true, true);
      }
    }
  }

  private void firePostInitializationHandlers()
      throws ServerException, ConflictException, NotFoundException, ForbiddenException {

    for (RegisteredProject project : projectConfigs.getAll()) {
      if (project.getBaseFolder() == null) {
        continue;
      }

      List<String> types = new ArrayList<>(project.getMixins());
      types.add(project.getType());

      for (String type : types) {
        Optional<ProjectInitHandler> hOptional = projectHandlers.getProjectInitHandler(type);
        if (hOptional.isPresent()) {
          hOptional.get().onProjectInitialized(project.getBaseFolder());
        }
      }
    }
  }
}
