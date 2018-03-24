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
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.watcher.server.FileWatcherManager;

/**
 * Cleans up project config registry when some project is removed bypassing {@link ProjectManager}
 *
 * @author Roman Nikitenko
 */
@Singleton
public class RootDirRemovalHandler {
  private final ProjectManager projectManager;
  private final FileWatcherManager fileWatcherManager;

  @Inject
  public RootDirRemovalHandler(
      ProjectManager projectManager, FileWatcherManager fileWatcherManager) {
    this.projectManager = projectManager;
    this.fileWatcherManager = fileWatcherManager;
  }

  @PostConstruct
  private void registerOperation() {
    fileWatcherManager.registerByPath(ROOT, null, null, this::consumeDelete);
  }

  private void consumeDelete(String wsPath) {
    try {
      if (projectManager.isRegistered(wsPath)) {
        projectManager.delete(wsPath);
      }
    } catch (ServerException | ForbiddenException | NotFoundException | ConflictException e) {
      // ignore
    }
  }
}
