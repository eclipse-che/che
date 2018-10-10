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
package org.eclipse.che.api.languageserver;

import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.languageserver.RegistryContainer.Registry;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifies registered language servers about file related events. Specifying of file event type is
 * expected to be defined through this class inheritance.
 *
 * @author Dmytro Kulieshov
 * @see <a
 *     href="https://microsoft.github.io/language-server-protocol/specification#workspace_didChangeWatchedFiles">DidChangeWatchedFiles
 *     Notification</a> section of LSP specification
 */
class LanguageServerAbstractFileWatcher implements Consumer<Path> {
  private static final Logger LOG =
      LoggerFactory.getLogger(LanguageServerAbstractFileWatcher.class);

  private final EventService eventService;
  private final Registry<Set<PathMatcher>> pathMatcherRegistry;
  private final FileChangeType fileChangeType;

  private final Map<PathMatcher, Consumer<Path>> eventConsumers = new ConcurrentHashMap<>();

  @Inject
  LanguageServerAbstractFileWatcher(
      EventService eventService, RegistryContainer registries, FileChangeType fileChangeType) {
    this.eventService = eventService;
    this.pathMatcherRegistry = registries.pathMatcherRegistry;
    this.fileChangeType = fileChangeType;
  }

  @PostConstruct
  protected void subscribe() {
    eventService.subscribe(
        event -> {
          Set<PathMatcher> pathMatchers = pathMatcherRegistry.getOrNull(event.getId());
          registerConsumers(event.getLanguageServer(), pathMatchers);
        },
        LanguageServerInitializedEvent.class);
  }

  @PreDestroy
  void removeAllWatchers() {
    eventConsumers.clear();
  }

  private void registerConsumers(LanguageServer languageServer, Set<PathMatcher> pathMatchers) {
    if (pathMatchers == null) {
      return;
    }

    for (PathMatcher pathMatcher : pathMatchers) {
      eventConsumers.put(
          pathMatcher,
          path -> {
            FileEvent event = new FileEvent(path.toString(), fileChangeType);
            List<FileEvent> changes = ImmutableList.of(event);
            DidChangeWatchedFilesParams params = new DidChangeWatchedFilesParams(changes);
            WorkspaceService service = languageServer.getWorkspaceService();
            service.didChangeWatchedFiles(params);
          });
    }
  }

  @Override
  public void accept(Path path) {
    for (Entry<PathMatcher, Consumer<Path>> entry : eventConsumers.entrySet()) {
      PathMatcher pathMatcher = entry.getKey();
      Consumer<Path> eventConsumer = entry.getValue();

      if (pathMatcher.matches(path)) {
        eventConsumer.accept(path);
      }
    }
  }
}
