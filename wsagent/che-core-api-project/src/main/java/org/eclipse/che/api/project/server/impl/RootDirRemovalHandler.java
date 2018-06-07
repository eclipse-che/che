/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.ProjectManager;
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

  private final ExecutiveProjectManager projectManager;
  private final FileWatcherManager fileWatcherManager;
  private final EventService eventService;

  @Inject
  public RootDirRemovalHandler(
      ExecutiveProjectManager projectManager,
      FileWatcherManager fileWatcherManager,
      EventService eventService) {
    this.projectManager = projectManager;
    this.fileWatcherManager = fileWatcherManager;
    this.eventService = eventService;
  }

  @PostConstruct
  private void registerOperation() {
    // handle removed root dirs
    fileWatcherManager.registerByPath(
        ROOT,
        arg -> {},
        arg -> {},
        wsPath -> {
          if (projectManager.isRegistered(wsPath)) {
            projectManager
                .getAll(wsPath)
                .stream()
                .map(ProjectConfig::getPath)
                .forEach(projectManager::ensureRemoved);
            projectManager.ensureRemoved(wsPath);
          }
        });
  }
}
