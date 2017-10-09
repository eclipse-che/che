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

import static org.eclipse.che.api.fs.server.WsPathUtils.ROOT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;

@Singleton
public class OnWorkspaceStartProjectInitializer {

  private final FsManager fsManager;
  private final ProjectSynchronizer projectSynchronizer;
  private final ProjectConfigRegistry projectConfigRegistry;
  private final ProjectHandlerRegistry projectHandlerRegistry;

  @Inject
  public OnWorkspaceStartProjectInitializer(
      FsManager fsManager,
      ProjectSynchronizer projectSynchronizer,
      ProjectConfigRegistry projectConfigRegistry,
      ProjectHandlerRegistry projectHandlerRegistry) {
    this.fsManager = fsManager;
    this.projectSynchronizer = projectSynchronizer;
    this.projectConfigRegistry = projectConfigRegistry;
    this.projectHandlerRegistry = projectHandlerRegistry;
  }

  @PostConstruct
  public void initialize()
      throws ConflictException, NotFoundException, ServerException, ForbiddenException {
    initializeRegisteredProjects();
    initializeNotRegisteredProjects();
    firePostInitializationHandlers();
  }

  private void initializeRegisteredProjects() throws ServerException {
    for (ProjectConfig projectConfig : projectSynchronizer.getAll()) {
      projectConfigRegistry.put(projectConfig, false, false);
    }
  }

  private void initializeNotRegisteredProjects() throws ServerException {
    for (String wsPath : fsManager.getDirWsPaths(ROOT)) {
      if (!projectConfigRegistry.isRegistered(wsPath)) {
        projectConfigRegistry.put(wsPath, true, true);
      }
    }
  }

  private void firePostInitializationHandlers()
      throws ServerException, ConflictException, NotFoundException, ForbiddenException {

    for (RegisteredProject project : projectConfigRegistry.getAll()) {
      if (project.getBaseFolder() == null) {
        continue;
      }

      List<String> types = new ArrayList<>(project.getMixins());
      types.add(project.getType());

      for (String type : types) {
        Optional<ProjectInitHandler> hOptional = projectHandlerRegistry.getProjectInitHandler(type);
        if (hOptional.isPresent()) {
          hOptional.get().onProjectInitialized(project.getBaseFolder());
        }
      }
    }
  }
}
