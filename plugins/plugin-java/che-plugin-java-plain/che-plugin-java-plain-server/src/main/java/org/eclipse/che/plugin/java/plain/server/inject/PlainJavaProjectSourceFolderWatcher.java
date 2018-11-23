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
package org.eclipse.che.plugin.java.plain.server.inject;

import static java.nio.file.Files.isDirectory;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.prefixURI;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.removeUriScheme;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.jdt.ls.extension.api.Commands.GET_PROJECT_SOURCE_LOCATIONS_COMMAND;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.languageserver.ExtendedLanguageServer;
import org.eclipse.che.api.languageserver.FindServer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.eclipse.che.api.watcher.server.impl.FileWatcherByPathMatcher;
import org.eclipse.che.plugin.java.inject.JavaModule;
import org.eclipse.che.plugin.java.languageserver.ProjectClassPathChangedEvent;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.FileChangeType;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Reports the create/update/delete changes on project source folders to jdt.ls
 *
 * @author V. Rubezhny
 */
public class PlainJavaProjectSourceFolderWatcher {
  private static final Gson gson =
      new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

  private final FileWatcherManager manager;
  private final FileWatcherByPathMatcher matcher;
  private final FindServer lsRegistry;
  private final ProjectManager projectManager;
  private final EventService eventService;
  private final CopyOnWriteArrayList<Integer> watcherIds = new CopyOnWriteArrayList<>();

  private PathTransformer pathTransformer;

  @Inject
  public PlainJavaProjectSourceFolderWatcher(
      FileWatcherManager manager,
      FileWatcherByPathMatcher matcher,
      FindServer lsRegistry,
      ProjectManager projectManager,
      EventService eventService,
      PathTransformer pathTransformer) {
    this.manager = manager;
    this.matcher = matcher;
    this.lsRegistry = lsRegistry;
    this.projectManager = projectManager;
    this.eventService = eventService;
    this.pathTransformer = pathTransformer;
  }

  @PostConstruct
  protected void startWatchers() {
    int watcherId =
        manager.registerByMatcher(
            folderMatcher(),
            s -> report(s, FileChangeType.Created),
            s -> {},
            s -> report(s, FileChangeType.Deleted));

    watcherIds.add(watcherId);
    eventService.subscribe(this::onProjectUpdated, ProjectClassPathChangedEvent.class);
  }

  @PreDestroy
  public void stopWatchers() {
    watcherIds.stream().forEach(id -> manager.unRegisterByMatcher(id));
  }

  private void onProjectUpdated(ProjectClassPathChangedEvent event) {
    ExecuteCommandParams params =
        new ExecuteCommandParams(
            GET_PROJECT_SOURCE_LOCATIONS_COMMAND, singletonList(prefixURI(event.getPath())));

    ExtendedLanguageServer languageServer = lsRegistry.byId(JavaModule.LS_ID);
    if (languageServer == null) {
      return;
    }

    languageServer
        .getServer()
        .getWorkspaceService()
        .executeCommand(params)
        .thenAccept(
            result -> {
              if (result == null) {
                return;
              }
              Type type = new TypeToken<ArrayList<String>>() {}.getType();
              List<String> paths = gson.fromJson(gson.toJson(result), type);
              paths.stream().forEach(f -> matcher.accept(Paths.get(removeUriScheme(prefixURI(f)))));
            });
  }

  private PathMatcher folderMatcher() {
    return it -> isDirectoryOfJavaProject(it);
  }

  private boolean isDirectoryOfJavaProject(Path path, LinkOption... options) {
    if (!isDirectory(path, options)) {
      return false;
    }
    RegisteredProject project = projectManager.getClosestOrNull(pathTransformer.transform(path));
    return project != null && project.getType().equals(JAVAC);
  }

  private void report(String path, FileChangeType changeType) {
    ExtendedLanguageServer languageServer = lsRegistry.byId(JavaModule.LS_ID);
    if (languageServer != null) {
      send(languageServer.getServer(), path, changeType);
    }
  }

  private void send(LanguageServer server, String path, FileChangeType changeType) {
    DidChangeWatchedFilesParams params =
        new DidChangeWatchedFilesParams(
            Collections.singletonList(new FileEvent(prefixURI(path), changeType)));
    server.getWorkspaceService().didChangeWatchedFiles(params);
  }
}
