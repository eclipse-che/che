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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.notification.ProjectDeletedEvent;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cleans up project config registry when some project is removed bypassing {@link ProjectManager}
 *
 * @author Roman Nikitenko
 */
@Singleton
public class RootDirRemovalHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(RootDirRemovalHandler.class);

  private final WorkspaceProjectSynchronizer projectSynchronizer;
  private final ProjectConfigRegistry projectConfigRegistry;
  private final FileWatcherManager fileWatcherManager;
  private final EventService eventService;

  @Inject
  public RootDirRemovalHandler(
      WorkspaceProjectSynchronizer projectSynchronizer,
      ProjectConfigRegistry projectConfigRegistry,
      FileWatcherManager fileWatcherManager,
      EventService eventService) {
    this.projectSynchronizer = projectSynchronizer;
    this.projectConfigRegistry = projectConfigRegistry;
    this.fileWatcherManager = fileWatcherManager;
    this.eventService = eventService;
  }

  @PostConstruct
  private void registerOperation() {
    fileWatcherManager.registerByPath(ROOT, arg -> {}, arg -> {}, this::consumeDelete);
  }

  private void consumeDelete(String wsPath) {
    try {
      if (projectConfigRegistry.isRegistered(wsPath)) {
        projectConfigRegistry
            .getAll(wsPath)
            .stream()
            .map(RegisteredProject::getPath)
            .forEach(projectConfigRegistry::remove);

        projectConfigRegistry
            .remove(wsPath)
            .map(RegisteredProject::getPath)
            .map(ProjectDeletedEvent::new)
            .ifPresent(eventService::publish);
        projectSynchronizer.synchronize();
      }
    } catch (ServerException e) {
      LOGGER.error(
          "Removing project '{}' is detected. Cleaning project config registry is failed: {}",
          wsPath,
          e.getMessage());
    }
  }
}
