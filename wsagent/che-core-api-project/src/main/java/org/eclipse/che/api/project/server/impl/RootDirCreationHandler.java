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

import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.search.server.excludes.HiddenItemPathMatcher;
import org.eclipse.che.api.watcher.server.FileWatcherManager;

/** Detects and makes directories created in VFS root as blank projects */
@Singleton
public class RootDirCreationHandler {
  private final FileWatcherManager fileWatcherManager;
  private final ProjectConfigRegistry projectConfigRegistry;
  private HiddenItemPathMatcher hiddenItemPathMatcher;

  @Inject
  public RootDirCreationHandler(
      FileWatcherManager fileWatcherManager,
      ProjectConfigRegistry projectConfigRegistry,
      HiddenItemPathMatcher hiddenItemPathMatcher) {
    this.fileWatcherManager = fileWatcherManager;
    this.projectConfigRegistry = projectConfigRegistry;
    this.hiddenItemPathMatcher = hiddenItemPathMatcher;
  }

  @PostConstruct
  private void registerOperation() {
    fileWatcherManager.registerByPath(ROOT, this::consumeCreate, null, null);
  }

  private void consumeCreate(String wsPath) {
    if (!hiddenItemPathMatcher.matches(Paths.get(wsPath))) {
      projectConfigRegistry.putIfAbsent(wsPath, true, true);
    }
  }
}
