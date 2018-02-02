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
package org.eclipse.che.api.languageserver.registry;

import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixURI;
import static org.eclipse.lsp4j.FileChangeType.Changed;
import static org.eclipse.lsp4j.FileChangeType.Created;
import static org.eclipse.lsp4j.FileChangeType.Deleted;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.consumers.LanguageServerFileChangeConsumer;
import org.eclipse.che.api.languageserver.consumers.LanguageServerFileCreateConsumer;
import org.eclipse.che.api.languageserver.consumers.LanguageServerFileDeleteConsumer;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
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
  private final LanguageServerFileCreateConsumer fileCreateConsumer;
  private final LanguageServerFileChangeConsumer fileUpdateConsumer;
  private final LanguageServerFileDeleteConsumer fileDeleteConsumer;

  @Inject
  public LanguageServerFileWatcher(
      LanguageServerFileCreateConsumer fileCreateConsumer,
      LanguageServerFileChangeConsumer fileUpdateConsumer,
      LanguageServerFileDeleteConsumer fileDeleteConsumer,
      LanguageServerRegistry serverInitializer) {
    this.fileCreateConsumer = fileCreateConsumer;
    this.fileUpdateConsumer = fileUpdateConsumer;
    this.fileDeleteConsumer = fileDeleteConsumer;

    serverInitializer.addObserver(this::onServerInitialized);
  }

  private void send(LanguageServer server, String filePath, FileChangeType changeType) {
    DidChangeWatchedFilesParams params =
        new DidChangeWatchedFilesParams(
            Collections.singletonList(new FileEvent(prefixURI(filePath), changeType)));
    server.getWorkspaceService().didChangeWatchedFiles(params);
  }

  private void onServerInitialized(
      LanguageServerLauncher launcher,
      LanguageServer server,
      ServerCapabilities capabilities,
      String projectPath) {
    LanguageServerDescription description = launcher.getDescription();
    Set<PathMatcher> matchers =
        description
            .getFileWatchPatterns()
            .stream()
            .map(patternToMatcher())
            .collect(Collectors.toSet());

    fileCreateConsumer.watch(s -> send(server, s, Created), matchers);
    fileUpdateConsumer.watch(s -> send(server, s, Changed), matchers);
    fileDeleteConsumer.watch(s -> send(server, s, Deleted), matchers);
  }

  static Function<String, PathMatcher> patternToMatcher() {
    FileSystem fileSystem = FileSystems.getDefault();
    return fileSystem::getPathMatcher;
  }
}
