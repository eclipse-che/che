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

import static org.eclipse.che.api.fs.server.FsPathResolver.ROOT;

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
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.handlers.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;

@Singleton
public class SimpleProjectInitializer implements ProjectInitializer {

  private final FsManager fileSystemManager;
  private final ProjectSynchronizer projectSynchronizer;
  private final ProjectConfigRegistry projectConfigRegistry;
  private final ProjectHandlerRegistry projectHandlers;

  @Inject
  public SimpleProjectInitializer(
      FsManager fileSystemManager,
      ProjectSynchronizer projectSynchronizer,
      ProjectConfigRegistry projectConfigRegistry,
      ProjectHandlerRegistry projectHandlers) {
    this.fileSystemManager = fileSystemManager;
    this.projectSynchronizer = projectSynchronizer;
    this.projectConfigRegistry = projectConfigRegistry;
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
      throws ServerException {
    for (ProjectConfig projectConfig : projectSynchronizer.getAll()) {
      projectConfigRegistry.put(projectConfig, false, false);
    }
  }

  private void initializeNotRegisteredProjects() throws ServerException {
    Set<String> wsPaths = fileSystemManager.getDirectoryWsPaths(ROOT);
    for (String wsPath : wsPaths) {
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
        Optional<ProjectInitHandler> hOptional = projectHandlers.getProjectInitHandler(type);
        if (hOptional.isPresent()) {
          hOptional.get().onProjectInitialized(project.getBaseFolder());
        }
      }
    }
  }
}
