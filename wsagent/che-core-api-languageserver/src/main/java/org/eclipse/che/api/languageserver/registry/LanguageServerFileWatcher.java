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
package org.eclipse.che.api.languageserver.registry;

import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixURI;

import com.google.common.annotations.VisibleForTesting;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Implement <a
 * href="https://github.com/Microsoft/language-server-protocol/blob/master/protocol.md#workspace_didChangeWatchedFiles">DidChangeWatchedFiles
 * Notification</a>
 */
@Singleton
public class LanguageServerFileWatcher {

  private final FileWatcherManager watcherManager;

  private CopyOnWriteArrayList<Integer> watcherIds = new CopyOnWriteArrayList<>();

  @Inject
  public LanguageServerFileWatcher(
      FileWatcherManager watcherManager, ServerInitializer serverInitializer) {
    this.watcherManager = watcherManager;
    serverInitializer.addObserver(this::onServerInitialized);
  }

  private void send(LanguageServer server, String filePath, FileChangeType changeType) {
    DidChangeWatchedFilesParams params =
        new DidChangeWatchedFilesParams(
            Collections.singletonList(new FileEvent(prefixURI(filePath), changeType)));
    server.getWorkspaceService().didChangeWatchedFiles(params);
  }

  @PreDestroy
  @VisibleForTesting
  public void removeAllWatchers() {
    for (Integer watcherId : watcherIds) {
      watcherManager.unRegisterByMatcher(watcherId);
    }
  }

  private void onServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String projectPath) {
    LanguageServerDescription description = launcher.getDescription();
    FileSystem fileSystem = FileSystems.getDefault();
    for (String pattern : description.getFileWatchPatterns()) {
      PathMatcher matcher = fileSystem.getPathMatcher(pattern);
      int watcherId =
          watcherManager.registerByMatcher(
              matcher,
              s -> send(server, s, FileChangeType.Created),
              s -> send(server, s, FileChangeType.Changed),
              s -> send(server, s, FileChangeType.Deleted));

      watcherIds.add(watcherId);
    }
  }
}
