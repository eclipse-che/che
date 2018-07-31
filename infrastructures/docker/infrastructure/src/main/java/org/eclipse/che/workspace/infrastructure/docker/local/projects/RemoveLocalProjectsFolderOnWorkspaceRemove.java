/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static java.lang.System.getenv;
import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener for removing projects files from host after {@code WorkspaceRemovedEvent}.
 *
 * <p>Note that projects folder won't be removed if host projects holder is not configured - this
 * listener is useful when Che master and Che workspaces are on the same host.
 *
 * @author Alexander Andrienko
 * @author Sergii Leshchenko
 * @see LocalProjectsFolderPathProvider#hostProjectsFolder
 */
@Singleton
public class RemoveLocalProjectsFolderOnWorkspaceRemove
    implements EventSubscriber<WorkspaceRemovedEvent> {

  private static final Logger LOG =
      LoggerFactory.getLogger(RemoveLocalProjectsFolderOnWorkspaceRemove.class);

  private final ExecutorService executor;
  private final LocalProjectsFolderPathProvider workspaceFolderPathProvider;

  @Inject(optional = true)
  @Named("host.projects.root")
  private String hostProjectsFolder;

  @Inject
  public RemoveLocalProjectsFolderOnWorkspaceRemove(
      LocalProjectsFolderPathProvider workspaceFolderPathProvider) {
    this.workspaceFolderPathProvider = workspaceFolderPathProvider;
    executor =
        Executors.newCachedThreadPool(
            new ThreadFactoryBuilder()
                .setNameFormat("RemoveLocalProjectsFolderOnWorkspaceRemove-%d")
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .build());
  }

  @VisibleForTesting
  RemoveLocalProjectsFolderOnWorkspaceRemove(
      String hostProjectFolder, LocalProjectsFolderPathProvider workspaceFolderPathProvider) {
    this(workspaceFolderPathProvider);
    this.hostProjectsFolder = hostProjectFolder;
  }

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this);
  }

  @Override
  public void onEvent(WorkspaceRemovedEvent event) {
    Workspace workspace = event.getWorkspace();
    String workspaceId = workspace.getId();

    String projectSourcesPath;
    try {
      projectSourcesPath = workspaceFolderPathProvider.getPath(workspaceId);
    } catch (IOException e) {
      LOG.error(
          "Failed to evaluate projects files path for workspace with id: '{}'. Cause: '{}'",
          workspace.getId(),
          e.getMessage());
      return;
    }

    if (projectSourcesPath.equals(hostProjectsFolder)) {
      // Do not remove folder because workspace uses shared folder for projects
      return;
    }

    // Remove the workspace folder located in the Che instance docker container.
    String mountedProjectSourcesPath = projectSourcesPath.replace(getInstanceDataPath(), "");
    deleteRecursiveAsync(workspace.getId(), mountedProjectSourcesPath);
  }

  @VisibleForTesting
  void deleteRecursiveAsync(String workspaceId, String toRemove) {
    executor.execute(
        () -> {
          File toRemoveFile = new File(toRemove);
          boolean success = deleteRecursive(toRemoveFile);
          if (!success) {
            LOG.error(
                "Failed to remove projects folder {} for workspace with id: '{}'.",
                toRemoveFile.getAbsolutePath(),
                workspaceId);
          }
        });
  }

  @VisibleForTesting
  String getInstanceDataPath() {
    return getenv("CHE_INSTANCE");
  }
}
