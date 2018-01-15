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
package org.eclipse.che.api.project.server.impl;

import static org.eclipse.che.api.fs.server.WsPathUtils.ROOT;

import java.nio.file.Paths;
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
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.search.server.excludes.HiddenItemPathMatcher;
import org.eclipse.che.api.project.server.notification.BeforeProjectInitializedEvent;
import org.eclipse.che.api.project.server.notification.ProjectInitializedEvent;

@Singleton
public class OnWorkspaceStartProjectInitializer {

  private final FsManager fsManager;
  private final WorkspaceProjectSynchronizer projectSynchronizer;
  private final ProjectConfigRegistry projectConfigRegistry;
  private final ProjectHandlerRegistry projectHandlerRegistry;
  private final EventService eventService;
  private final HiddenItemPathMatcher hiddenItemPathMatcher;

  @Inject
  public OnWorkspaceStartProjectInitializer(
      FsManager fsManager,
      WorkspaceProjectSynchronizer projectSynchronizer,
      ProjectConfigRegistry projectConfigRegistry,
      ProjectHandlerRegistry projectHandlerRegistry,
      HiddenItemPathMatcher hiddenItemPathMatcher,
      ProjectHandlerRegistry projectHandlerRegistry,
      EventService eventService) {
    this.fsManager = fsManager;
    this.projectSynchronizer = projectSynchronizer;
    this.projectConfigRegistry = projectConfigRegistry;
    this.projectHandlerRegistry = projectHandlerRegistry;
    this.hiddenItemPathMatcher = hiddenItemPathMatcher;
    this.eventService = eventService;
  }

  @PostConstruct
  public void initialize()
      throws ConflictException, NotFoundException, ServerException, ForbiddenException {
    initializeRegisteredProjects();
    initializeNotRegisteredProjects();
    firePostInitializationHandlers();
  }

  private void initializeRegisteredProjects()
      throws ServerException, NotFoundException, ConflictException {
    for (ProjectConfig projectConfig : projectSynchronizer.getProjects()) {
      eventService.publish(new BeforeProjectInitializedEvent(projectConfig));
      projectConfigRegistry.put(projectConfig, false, false);
    }
  }

  private void initializeNotRegisteredProjects() {
    fsManager
        .getDirWsPaths(ROOT)
        .stream()
        .filter(wsPath -> !hiddenItemPathMatcher.matches(Paths.get(wsPath)))
        .forEach(wsPath -> projectConfigRegistry.putIfAbsent(wsPath, true, true));
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

      eventService.publish(new ProjectInitializedEvent(project.getBaseFolder()));
    }
  }
}
